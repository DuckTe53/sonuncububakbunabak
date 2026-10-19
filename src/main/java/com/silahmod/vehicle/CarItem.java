package com.silahmod.vehicle;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CarItem extends Item {
    private final int carClass;
    public CarItem(Properties properties, int carClass) { super(properties); this.carClass = carClass; }
    @Override public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        CarEntity car = new CarEntity(Cars.CAR, level);
        Vec3 p = context.getClickLocation().add(0, 0.15, 0);
        car.setPos(p.x, p.y, p.z); car.setYRot(context.getRotation().y); car.setCarClass(carClass);
        level.addFreshEntity(car);
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var hit = player.pick(5.0, 0, false);
        CarEntity car = new CarEntity(Cars.CAR, level);
        Vec3 p = hit.getLocation().add(0, 0.1, 0);
        car.setPos(p.x, p.y, p.z); car.setYRot(player.getYRot()); car.setCarClass(carClass); level.addFreshEntity(car);
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        return InteractionResult.CONSUME;
    }
}
