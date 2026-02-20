package com.luolian.stellarmod.example.java;

public abstract class RecodeClassOperation {
    /**
     * <h1>记录类</h1>
     * <h2>概述</h2>
     * 记录类是指那些主要用于存储数据的类。它们通常包含一些字段和相应的getter方法，但很少包含复杂的逻辑。<br>
     * 记录类的主要目的是为了简化数据的存储和传输，减少样板代码的编写，特性包含：
     * 记录类会自动生成以下内容：
     * <ul>
     *     <li>构造函数（Constructor）：记录类会自动生成一个构造器，形参为所有字段的值。</li>
     *     <li>访问器（getter）方法：记录类会为每个字段自动生成一个访问器方法（getter），方法名与字段名相同。</li>
     *     <li>toString方法：记录类会自动生成一个toString方法，返回一个字符串表示记录类的内容。</li>
     *     <li>equals和hashCode方法：记录类会自动生成equals和hashCode方法，用于实例比较和哈希计算。</li>
     * </ul>
     * 记录类会隐式地：
     * <ul>
     *     <li>继承自{@linkplain Record}类。</li>
     *     <li>被final关键字修饰，即无法被继承。</li>
     *     <li>使所有字段被private和final修饰。</li>
     * </ul>
     * <h2>示例</h2>
     * 想象这样一个场景，要设计一个包含用户的数据的类，通常我们会这样做：
     * <pre>{@code
     * public class User {
     *     private final String name;
     *     private final int age;
     *
     *     public User(String name, int age) {
     *         this.name = name;
     *         this.age = age;
     *     }
     *
     *     public String getName() {
     *         return name;
     *     }
     *
     *     public int getAge() {
     *         return age;
     *     }
     * }
     * }</pre>
     * 但是要加更多字段，要分别写构造函数和getter方法，不仅冗长还千篇一律。使用记录类，我们可以这样写：
     * <pre>{@code
     * public record User(String name, int age) {}
     * }</pre>
     * 没错，就是这么简单！记录类自动为我们生成了构造函数和getter方法，代码更简洁、更易读。
     * 使用时就像这样：
     * <pre>{@code
     * User user = new User("Alice", 30);
     * System.out.println(user.name()); // 输出: Alice
     * System.out.println(user.age());  // 输出: 30
     * }</pre>
     */
    @Deprecated
    public abstract void recordClassConcept();
}
