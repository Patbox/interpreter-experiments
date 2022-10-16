package eu.pb4.lang.parser;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

public final class VariableId2IntMapper {
    @Nullable
    private final VariableId2IntMapper parent;
    private final Object2IntMap<String> stringToId = new Object2IntOpenHashMap<>();
    private int currentId;
    private int highest;

    public VariableId2IntMapper(VariableId2IntMapper parent, int currentId) {
        this.parent = parent;
        this.currentId = currentId;
    }

    public int declare(String id) {
        var exist = this.get(id);

        if (exist == -1) {
            var val = currentId++;

            this.setHighest(val);
            this.stringToId.put(id, val);
            return val;
        } else {
            return exist;
        }
    }

    private void setHighest(int val) {
        if (this.parent != null) {
            this.parent.setHighest(val);
        } else {
            this.highest = Math.max(this.highest, val);
        }
    }

    public int getHighest() {
        return this.highest;
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
