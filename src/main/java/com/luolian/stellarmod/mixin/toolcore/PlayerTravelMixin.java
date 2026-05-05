package com.luolian.stellarmod.mixin.toolcore;

import com.luolian.stellarmod.server.item.custom.ToolCoreItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 注入 Player.travel() 尾部，在飞行移动计算完成后检测急停矩阵效果，
 * 若无水平输入则将速度归零，实现客户端预测环境下的惯性消除。
 * <p>
 * 必须通过 Mixin 实现而非 TickEvent，因为：
 * travel() 每 tick 重新计算速度，END 阶段 TickEvent 在其后触发，
 * 此时速度已被覆盖；START 阶段 TickEvent 设零后会被 travel() 覆盖。
 */
//告诉 Mixin 框架这个类要混入 net.minecraft.world.entity.player.Player
@Mixin(Player.class)
public class PlayerTravelMixin {

    //在 Player.travel(Vec3) 方法的末尾插入额外代码。TAIL 意味着在所有原版移动逻辑执行完之后运行
    @Inject(method = "travel", at = @At("TAIL"))

    //onTravelTail 是注入方法，CallbackInfo ci 用于控制回调（这里没用到）
    private void onTravelTail(Vec3 travelVector, CallbackInfo ci) {

    /*  在 Mixin 类中，this 表面上是 Mixin 自身，但运行时实际是目标类（Player）的实例，需要强制转换后才能调用 Player 的方法。标准写法就是 (Player) (Object) this
        在 Mixin 中，由于 this 的实际类型是目标类（Player）与 Mixin 类（PlayerTravelMixin）的交织，
           直接 (Player) this 会被 Java 编译器视为两个不兼容类型之间的强制转换，导致编译错误。
           必须先转 Object 是因为 Java 允许任何引用类型先向 Object 转型（这是一个宽化转换，总是合法），再从 Object 向下转型到 Player（这是一个窄化转换，运行时检查），
           这样编译器才不会报错。
        简单说：(Player) (Object) this 是 Mixin 中获取目标类实例的惯用写法，并非运行时需要，而是为了让编译器通过
        “交织”在这里指的是 Mixin 技术背后的编译期代码织入（Weaving）行为。
        具体来说：
            源代码层面：PlayerTravelMixin 和 Player 是两个独立的 .java 文件。PlayerTravelMixin 里写了 private void onTravelTail(...)。
            编译/加载时：Mixin 框架（通过注解处理器和类加载器）会将 PlayerTravelMixin 中的方法注入到 Player.class 的字节码中。最终在 JVM 里运行的 Player 类，
                其实是 原版 Player 的代码 + Mixin 代码合并后的产物。
            对 this 的影响：注入后的 onTravelTail 方法在运行时属于 Player 实例，this 指向的就是当前玩家对象。
                但在源码阶段，onTravelTail 是写在 PlayerTravelMixin 里的，编译器看到的 this 类型是 PlayerTravelMixin。
                这种“源码是分开的，但运行时合并成一个类”的状态，就是术语里说的交织（Weaving）。正因为编译器和运行时看到的 this 类型不一致，
                才需要用 (Object) 先“骗过”编译器，让强制转换通过。
    */
        Player self = (Player) (Object) this;

        //非飞行状态不干预
        if (!self.getAbilities().flying) return;

        //存在水平输入时不干预（自动急停仅在无输入时生效）
        if (self.xxa != 0.0F || self.zza != 0.0F) return;

        //扫描背包查找已启用的惯性消除矩阵
        for (ItemStack stack : self.getInventory().items) {
            if (stack.getItem() instanceof ToolCoreItem) {
                if (ToolCoreItem.isMatrixEnabled(stack, "stellarmod:inertia_cancellation")) {
                    int level = ToolCoreItem.getMatrixActiveLevel(stack, "stellarmod:inertia_cancellation");
                    if (level > 0) {
                        self.setDeltaMovement(Vec3.ZERO);
                        return;
                    }
                }
            }
        }
    }
}
