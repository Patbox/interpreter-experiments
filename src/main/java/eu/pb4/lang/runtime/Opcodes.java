package eu.pb4.lang.runtime;

import eu.pb4.lang.exception.InvalidOperationException;
import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.*;
import eu.pb4.lang.util.GenUtils;

import java.io.IOException;
import java.util.Arrays;

public enum Opcodes {
    DEBUG((runtime, scope, stack, buffer) -> {
        var y = System.nanoTime() - stack.instructionStartNanoTime;
        var x = GenUtils.readIdentifierString(buffer);
        switch (x) {
            case "printStack" -> {
                stack.debugPrint();
            }

            case "opcodes_true" -> stack.printOpcodes = true;
            case "opcodes_false" -> stack.printOpcodes = false;
            case "time_true" -> stack.printTime = true;
            case "time_false" -> stack.printTime = false;
            case "hit_time" -> System.out.println("Debug hit time: " + y);
            default -> {
                System.out.println("Debug: " + x);
            }
        }
    }),
    RETURN((runtime, scope, stack, buffer) -> {
        stack.finished = true;
    }),
    DUPLICATE_TOP((runtime, scope, stack, buffer) -> stack.push(stack.peek())),
    STACK_POP((runtime, scope, stack, buffer) -> stack.pop()),
    STACK_CLEAR((runtime, scope, stack, buffer) -> stack.clear()),

    DECLARE_VAR((runtime, scope, stack, buffer) -> {
        scope.declareVariable("", buffer.readUShort(), stack.peek(), buffer.readByte() == 1);
    }),
    SET_VAR((runtime, scope, stack, buffer) -> scope.setVariable(buffer.readUShort(), stack.peek())),
    SET_VAR_OLD((runtime, scope, stack, buffer) -> {
        var id = buffer.readUShort();
        var old = scope.getVariable(id);
        scope.setVariable(id, stack.pop());
        stack.push(old);
    }),
    GET_VAR((runtime, scope, stack, buffer) -> stack.push(scope.getVariable(buffer.readUShort()))),

    CALL_FUNC((runtime, scope, stack, buffer) -> {
        var args = stack.pop(buffer.read());
        stack.push(stack.pop().call(scope, args, stack.info));
    }),
    GET_PROPERTY((runtime, scope, stack, buffer) -> {
        var obj = stack.pop();
        stack.push(obj.get(scope, GenUtils.readIdentifierString(buffer), stack.info));
    }),
    SET_PROPERTY((runtime, scope, stack, buffer) -> {
        var val = stack.pop();
        var obj = stack.pop();
        obj.set(scope, GenUtils.readIdentifierString(buffer), val, Expression.Position.EMPTY);
        stack.push(val);
    }),
    SET_PROPERTY_OLD((runtime, scope, stack, buffer) -> {
        var val = stack.pop();
        var obj = stack.pop();
        var strn = GenUtils.readIdentifierString(buffer);
        var old = obj.get(scope, strn, Expression.Position.EMPTY);
        obj.set(scope, strn, val, Expression.Position.EMPTY);
        stack.push(old);
    }),

    SCOPE_UP((runtime, scope, stack, buffer) -> {
        stack.scope = new ObjectScope(scope);
        stack.clear();
    }),
    SCOPE_DOWN((runtime, scope, stack, buffer) -> {
        stack.scope = scope.getParent();
        stack.clear();
    }),
    SCOPE_STATE_SAVE((runtime, scope, stack, buffer) -> scope.storeState()),
    SCOPE_STATE_RESTORE((runtime, scope, stack, buffer) -> scope.restoreState()),
    SCOPE_STATE_RESTORE_REMOVE((runtime, scope, stack, buffer) -> scope.restoreAndRemoveState()),

    JUMP((runtime, scope, stack, buffer) -> {
        var id = buffer.readInt();
        buffer.pos += id;
    }),
    JUMP_IF_TRUE((runtime, scope, stack, buffer) -> {
        var id = buffer.readInt();
        if (stack.pop() == BooleanObject.TRUE) {
            buffer.pos += id;
        }
    }),
    JUMP_IF_FALSE((runtime, scope, stack, buffer) -> {
        var id = buffer.readInt();
        if (stack.pop() == BooleanObject.FALSE) {
            buffer.pos += id;
        }
    }),

    GET((runtime, scope, stack, buffer) -> {
        var key = stack.pop();
        var obj = stack.pop();
        stack.push(obj.get(scope, key, stack.info));
    }),
    SET((runtime, scope, stack, buffer) -> {
        var val = stack.pop();
        var key = stack.pop();
        var obj = stack.pop();

        obj.set(scope, key, val, Expression.Position.EMPTY);
        stack.push(val);
    }),
    SET_OLD((runtime, scope, stack, buffer) -> {
        var val = stack.pop();
        var key = stack.pop();
        var obj = stack.pop();

        var old = obj.get(scope, key, Expression.Position.EMPTY);
        obj.set(scope, key, val, Expression.Position.EMPTY);
        stack.push(old);
    }),

    ADD((runtime, scope, stack, buffer) -> stack.push(stack.pop().add(scope, stack.pop(), stack.info))),

    SUBTRACT((runtime, scope, stack, buffer) -> stack.push(stack.pop().subtract(scope, stack.pop(), stack.info))),
    MULTIPLY((runtime, scope, stack, buffer) -> stack.push(stack.pop().multiply(scope, stack.pop(), stack.info))),
    DIVIDE((runtime, scope, stack, buffer) -> stack.push(stack.pop().divide(scope, stack.pop(), stack.info))),
    DIVIDE_REST((runtime, scope, stack, buffer) -> stack.push(stack.pop().divideRest(scope, stack.pop(), stack.info))),
    POWER((runtime, scope, stack, buffer) -> stack.push(stack.pop().power(scope, stack.pop(), stack.info))),

    LESS_THAN((runtime, scope, stack, buffer) -> stack.push(BooleanObject.of(stack.pop().lessThan(scope, stack.pop(), stack.info)))),
    LESS_OR_EQUAL((runtime, scope, stack, buffer) -> stack.push(BooleanObject.of(stack.pop().lessOrEqual(scope, stack.pop(), stack.info)))),
    EQUAL((runtime, scope, stack, buffer) -> stack.push(BooleanObject.of(stack.pop().equalsObj(scope, stack.pop(), stack.info)))),

    SHIFT_LEFT((runtime, scope, stack, buffer) -> stack.push(stack.pop().shiftLeft(scope, stack.pop(), stack.info))),
    SHIFT_RIGHT((runtime, scope, stack, buffer) -> stack.push(stack.pop().shiftRight(scope, stack.pop(), stack.info))),

    NEGATE((runtime, scope, stack, buffer) -> stack.push(stack.pop().negate(scope, stack.info))),
    AND((runtime, scope, stack, buffer) -> stack.push(stack.pop().and(scope, stack.pop(), stack.info))),
    OR((runtime, scope, stack, buffer) -> stack.push(stack.pop().or(scope, stack.pop(), stack.info))),

    ITERATOR((runtime, scope, stack, buffer) -> stack.push(new IteratorObject(stack.pop().iterator(scope, stack.info)))),
    TYPE_OF((runtime, scope, stack, buffer) -> stack.push(new StringObject(stack.pop().type()))),

    GET_GLOBAL((runtime, scope, stack, buffer) -> stack.push(runtime.getGlobal(GenUtils.readIdentifierString(buffer)))),
    IMPORT((runtime, scope, stack, buffer) -> {
        var key = stack.pop();
        stack.push(runtime.importAndRun(key.asString()));
    }),
    EXPORT((runtime, scope, stack, buffer) -> {
        var key = stack.pop();
        var value = stack.pop();
        scope.addExport(key.asString(), value);
    }),

    PUSH_CONSTANT((runtime, scope, stack, buffer) -> stack.push(scope.getConstant(buffer.readInt()))),
    PUSH_DOUBLE((runtime, scope, stack, buffer) -> stack.push(NumberObject.of(buffer.readDouble()))),
    PUSH_INT((runtime, scope, stack, buffer) -> stack.push(NumberObject.of(buffer.readInt()))),
    PUSH_STRING((runtime, scope, stack, buffer) -> {
        var x = new byte[buffer.readInt()];
        buffer.read(x);
        stack.push(new StringObject(new String(x)));
    }),
    PUSH_BOOLEAN((runtime, scope, stack, buffer) -> stack.push(BooleanObject.of(buffer.read() == 1))),
    PUSH_NULL((runtime, scope, stack, buffer) -> stack.push(XObject.NULL)),
    PUSH_LIST((runtime, scope, stack, buffer) -> {
        var list = new ListObject();
        var count = buffer.readUShort();

        while (count-- > 0) {
            list.asJava().add(stack.pop());
        }

        stack.push(list);
    }),
    PUSH_MAP((runtime, scope, stack, buffer) -> {
        var list = new MapObject();
        var count = buffer.readUShort();

        while (count-- > 0) {
            list.asJava().put(stack.pop(), stack.pop());
        }

        stack.push(list);
    }),
    PUSH_OBJECT((runtime, scope, stack, buffer) -> {
        var list = new StringMapObject();
        var count = buffer.readUShort();

        while (count-- > 0) {
            list.asJava().put(stack.pop().asString(), stack.pop());
        }

        stack.push(list);
    }),
    PUSH_CLASS((runtime, scope, stack, buffer) -> {}),

    PUSH_FUNCTION((runtime, scope, stack, buffer) -> {
        var argCount = buffer.read();
        var ids = new int[argCount];

        for (int i = 0; i < argCount; i++) {
            ids[i] = buffer.readUShort();
        }

        var funcBytes = new byte[buffer.readInt()];
        buffer.read(funcBytes);

        stack.push(new ByteCodeFunctionObject(new ObjectScope(scope), ids, funcBytes));
    });

    public final OpcodeCall call;

    Opcodes(OpcodeCall call) {
        this.call = call;
    }

    public static OpcodeCall[] CALLS = Arrays.stream(Opcodes.values()).map(x -> x.call).toArray(OpcodeCall[]::new);

    @FunctionalInterface
    public interface OpcodeCall {
        void call(Runtime runtime, ObjectScope scope, RuntimeStack stack, ByteArrayReader buffer) throws IOException, InvalidOperationException;
    }

    static {
        System.out.println("Opcodes");
        for (int i = 0; i < values().length; i++) {
            System.out.println(i + " | " + values()[i]);
        }
    }
}
