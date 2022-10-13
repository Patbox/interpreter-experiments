package eu.pb4.lang.parser;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

public final class VariableId2IntMapper {
    @Nullable
    private final VariableId2IntMapper parent;
    private final Object2IntMap<String> stringToId = new Object2IntOpenHashMap<>();
    private int currentId;

    public VariableId2IntMapper(VariableId2IntMapper parent, int currentId) {
        this.parent = parent;
        this.currentId = currentId;
    }

    public int declare(String id) {
        var exist = this.get(id);

        if (exist == -1) {
            var val = currentId++;
            this.stringToId.put(id, val);
            return val;
        } else {
            return exist;
        }
    }

    public int get(String id) {
        var x = this.stringToId.getOrDefault(id, -1);

        if (x == -1 && this.parent != null) {
            return this.parent.get(id);
        }

        return x;
    }

    public VariableId2IntMapper up() {
        return new VariableId2IntMapper(this, this.currentId);
    }
}
