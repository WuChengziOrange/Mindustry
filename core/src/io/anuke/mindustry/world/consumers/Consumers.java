package io.anuke.mindustry.world.consumers;

import io.anuke.arc.function.Predicate;
import io.anuke.arc.util.Structs;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.blocks.power.ConditionalConsumePower;
import io.anuke.mindustry.world.meta.BlockStats;

public class Consumers{
    private Consume[] map = new Consume[ConsumeType.values().length];
    private Consume[] results, optionalResults;

    public final boolean[] itemFilters = new boolean[Vars.content.items().size];
    public final boolean[] liquidfilters = new boolean[Vars.content.liquids().size];

    public void init(){
        results = Structs.filter(Consume.class, map, m -> m != null);
        optionalResults = Structs.filter(Consume.class, map, m -> m != null && m.isOptional());

        for(Consume cons : results){
            cons.applyItemFilter(itemFilters);
            cons.applyLiquidFilter(liquidfilters);
        }
    }

    public ConsumePower getPower(){
        return get(ConsumeType.power);
    }

    public boolean hasPower(){
        return has(ConsumeType.power);
    }

    public ConsumeLiquid liquid(Liquid liquid, float amount){
        return add(new ConsumeLiquid(liquid, amount));
    }

    /**
     * Creates a consumer which directly uses power without buffering it.
     * @param powerPerTick The amount of power which is required each tick for 100% efficiency.
     * @return the created consumer object.
     */
    public ConsumePower power(float powerPerTick){
        return add(new ConsumePower(powerPerTick, 0.0f, false));
    }

    /** Creates a consumer which only consumes power when the condition is met. */
    public ConsumePower powerCond(float usage, Predicate<TileEntity> cons){
        return add(new ConditionalConsumePower(usage, cons));
    }

    /**
     * Creates a consumer which stores power.
     * @param powerCapacity The maximum capacity in power units.
     */
    public ConsumePower powerBuffered(float powerCapacity){
        return add(new ConsumePower(0f, powerCapacity, true));
    }

    public ConsumeItems item(Item item){
        return item(item, 1);
    }

    public ConsumeItems item(Item item, int amount){
        return add(new ConsumeItems(new ItemStack[]{new ItemStack(item, amount)}));
    }

    public ConsumeItems items(ItemStack... items){
        return add(new ConsumeItems(items));
    }

    public <T extends Consume> T add(T consume){
        map[consume.type().ordinal()] = consume;
        return consume;
    }

    public void remove(ConsumeType type){
        map[type.ordinal()] = null;
    }

    public boolean has(ConsumeType type){
        return map[type.ordinal()] != null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Consume> T get(ConsumeType type){
        if(map[type.ordinal()] == null){
            throw new IllegalArgumentException("Block does not contain consumer of type '" + type + "'!");
        }
        return (T)map[type.ordinal()];
    }

    public Consume[] all(){
        return results;
    }

    public Consume[] optionals(){
        return optionalResults;
    }

    public void display(BlockStats stats){
        for(Consume c : map){
            if(c != null){
                c.display(stats);
            }
        }
    }
}
