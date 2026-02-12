package com.luolian.stellarmod.example;

import net.minecraft.nbt.CompoundTag;

/**
 * <h1>基础操作</h1>
 * 其中演示了开发过程中一些基础的操作，比如序列化与反序列化（数据持久化）。
 */
public abstract class BaseOperation {
    /**
     * <h1>序列化与反序列化（数据持久化）</h1>
     * <h2>概述</h2>
     * 序列化是将对象转换为可存储或传输的格式的过程。<br>
     * 反序列化是将已存储的格式转换回对象的过程。<br>
     * 这部分细节属于基础知识，不在这里展开。<br>
     * <br>
     * NBT标签（Named Binary Tag）是一种用于存储数据的格式，广泛应用于Minecraft中。<br>
     * 直接解释NBT会很抽象，我们可以用json结构来类比和可视化NBT的结构。<br>
     * 如下：
     * <pre>{@code
     * "": { // 根标签
     *     Attributes: [
     *         {
     *             Base: 1.0, // 基础值
     *             Name: "minecraft:generic.attack_damage" // 攻击伤害
     *             Modifiers: [
     *                 {
     *                     Name: "xxx", // 修饰符名称
     *                     Amount: 1.0, // 修饰值
     *                     Operation: 0, // 操作类型
     *                     UUID: [ 1, 2, 3, 4 ] // 修饰符的UUID
     *                 }
     *             ]
     *         },
     *         {
     *             Base: 4.0, // 基础值
     *             Name: "minecraft:generic.attack_speed" // 攻击速度
     *         }
     *     ],
     *     AbsorptionAmount: 0.0F, // 伤害吸收量
     *     Air: 300S, // 空气值
     *     Pos: [
     *         39.31221322097955, // X坐标
     *         -38.0, // Y坐标
     *         -55.247578739566286, // Z坐标
     *     ]
     * }
     * }</pre>
     * 之所以可以用json类比，是因为NBT和其一样是树形结构，使用键值对来标记数据，
     * NBT标签对应json对象，NBT数组也对应json数组，在语法上很相似。<br>
     * <br>
     * 在单维使用时可以想象其是一个{@code Map<String, Object>}，我们可以put和get数据。<br>
     * <h2>触发时机</h2>
     * 序列化和反序列化都在服务端进行，客户端不存在这些概念。<br>
     * 序列化的触发时机通常是服务器暂停、服务器关闭、玩家离开服务器和自动保存时等。<br>
     * 反序列化的触发时机通常是服务器启动、区块加载时等。<br>
     * <h2>创建NBT标签（序列化）</h2>
     * 以下是创建NBT标签的方式：
     * <pre>{@code
     * private float floatValue = 0.0f;
     * private short shortValue = 300;
     * private boolean booleanValue = false;
     * // 一般这里的形参是父标签，或者根标签
     * // 你需要确保读取时的形参和写入时的形参是同一个NBT标签，否则在读取时将无法找到
     * public void writeNBTMethod(CompoundTag tag) {
     *     // 创建一个NBT标签对象
     *     CompoundTag customTag = new CompoundTag();
     *     // 向NBT标签中添加数据
     *     customTag.putFloat("FloatValue", this.floatValue);
     *     customTag.putShort("ShortValue", this.shortValue);
     *     customTag.putBoolean("BooleanValue", this.booleanValue);
     *     // 向父标签中添加刚刚创建的标签
     *     tag.put("CustomTag", customTag);
     * }
     * }</pre>
     * 存储后，使用json结构类比时，它们看起来就像这样：
     * <pre>{@code
     * {
     *     "CustomTag": {
     *         "FloatValue": 0.0,
     *         "ShortValue": 300,
     *         "BooleanValue": 0B
     *     }
     * }
     * }</pre>
     * <h2>读取NBT标签（反序列化）</h2>
     * 以下是读取NBT标签的方式：
     * <pre>{@code
     * // 一般这里的形参是父标签，或者根标签
     * // 你需要确保读取时的形参和写入时的形参是同一个NBT标签，否则在读取时将无法找到
     * public void readNBTMethod(CompoundTag tag) {
     *     // 非空检查
     *     if (tag.contains("CustomTag")) {
     *         // 从父标签中读取刚刚自定义的标签
     *         CompoundTag customTag = tag.getCompound("CustomTag");
     *         // 获取后可以赋值
     *         this.floatValue = customTag.getFloat("FloatValue");
     *         this.shortValue = customTag.getShort("ShortValue");
     *         this.booleanValue = customTag.getBoolean("BooleanValue");
     *         // getter方法总是有一个默认值，比如0或new ArrayList<>()
     *         // 当getter没有找到对应的键时，它会返回这个默认值
     * }
     * }</pre>
     * @param tag 一个NBT标签对象
     */
    @Deprecated
    public abstract void serializationConcept(CompoundTag tag);

    /**
     * <h1>实体</h1>
     * <h2>概述</h2>
     * 实体的概念并非仅指Minecraft中的可以动的生物，它是一个更广泛的概念。<br>
     * 其用于管理每个游戏内容自己的信息或功能。<br>
     * <h3>方块</h3>
     * Block类是用于表示具体方块的存在形式，但它并不包含该方块的位置、状态信息或特殊的功能，
     * 这些信息由BlockEntity类管理，即方块实体。<br>
     * <h3>物品</h3>
     * 相应的，Item类是用于表示具体物品的存在形式，但它并不包含该物品的数量、耐久度或特殊的功能，
     * 这些信息由ItemStack类管理，即物品堆栈。<br>
     * 注：ItemEntity对应的是掉落在地上的物品，这点容易混淆。<br>
     * <h2>总结</h2>
     * 还是以方块为例子。<br>
     * 具体的类（Block.class）定义了它如何存在，即它应该是什么，比如方块的硬度、贴图等。<br>
     * 而实体（BlockEntity.class）则定义了它如何表现，即它应该有什么，比如方块的坐标、状态等。<br>
     * @see net.minecraft.world.level.block.Block
     * @see net.minecraft.world.level.block.entity.BlockEntity
     * @see net.minecraft.world.item.Item
     * @see net.minecraft.world.item.ItemStack
     * @see net.minecraft.world.entity.item.ItemEntity
     */
    @Deprecated
    public abstract void entityConcept();
}
