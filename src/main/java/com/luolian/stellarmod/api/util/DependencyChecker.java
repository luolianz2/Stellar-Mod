package com.luolian.stellarmod.api.util;

import dev.ftb.mods.ftbquests.quest.QuestObject;

@FunctionalInterface
public interface DependencyChecker {
    boolean check(QuestObject questObject);

    default boolean checkCondition(QuestObject questObject) {
        return questObject.isValid() && this.check(questObject);
    }
}
