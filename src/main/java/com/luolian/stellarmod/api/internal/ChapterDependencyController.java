package com.luolian.stellarmod.api.internal;

import dev.ftb.mods.ftbquests.quest.DependencyRequirement;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;

import java.util.List;

public interface ChapterDependencyController {
    boolean stellar$hasDependencies();
    void stellar$addDependency(QuestObject dependency);
    void stellar$clearDependencies();
    boolean stellar$isInvisibleUntilDependenciesCompleted();
    void stellar$setInvisibleUntilDependenciesCompleted(boolean invisible);
    boolean stellar$isLockUntilDependenciesCompleted();
    void stellar$setLockUntilDependenciesCompleted(boolean lock);
    List<QuestObject> stellar$getDependencies();
    void stellar$setDependencyRequirement(DependencyRequirement requirement);
    DependencyRequirement stellar$getDependencyRequirement();
    boolean stellar$isLocked(TeamData teamData);
}
