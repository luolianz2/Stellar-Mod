package com.luolian.stellarmod.example.mixins.mixin_extras;

public abstract class MixinExtras {
    /**
     * <h1>MixinExtras</h1>
     * MixinExtras的MC百科页面：<a href="https://www.mcmod.cn/class/12750.html">MixinExtras</a><br>
     * <h2>这是什么？</h2>
     * MixinExtras是一个用于扩展Mixin功能的库，其可以帮助我们在更具表现力、更方便以及更好兼容性形式下编写 Mixin。
     * 最重要的是其中的{@code @WrapOperation}注解，由于{@code @Redirect}注解的兼容性问题，该注解<b>几乎</b>是其完美的上位替代品。
     * <h2>如何使用？</h2>
     * 直接在build.gradle中这样做就行了：
     * <pre>{@code
     * dependencies {
     *     // 这会让MixinExtras被以jar-in-jar的形式打包到你的mod中，无需额外安装
     *     implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:0.2.0"))
     *     implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.2.0")) {
     *         jarJar.ranged(it, "[0.2.0,)")
     *     }
     * }
     * }</pre>
     * <h2>题外话</h2>
     * MixinExtras在Fabric 0.15.0+、NeoForge 20.2.84-beta+中
     * 已经被作为核心库的一部分引入了，可见其重要性和实用性。
     * <h2>为什么我希望引入他？</h2>
     * <ul>
     *     <li>上面的{@code @WrapOperation}注解就不必多说了。</li>
     *     <li>其还提供了{@code @Local}注解，用于简单地获取可见的局部变量。</li>
     *     <li>还有cancel</li>
     * </ul>
     */
    @Deprecated
    public abstract void mixinExtras();
}
