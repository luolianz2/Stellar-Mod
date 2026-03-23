package com.luolian.stellarmod.mixin.compat;

import com.luolian.stellarmod.api.internal.ChapterDependencyController;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TeamData.class, remap = false)
public abstract class TeamDataMixin {
    @Inject(
            method = "setProgress",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetProgressHead(Task task, long progress, CallbackInfo ci) {
        if (this.stellar$cannotUpdate(task.getQuestChapter())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "canStartTasks",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCanStartTasksHead(Quest quest, CallbackInfoReturnable<Boolean> cir) {
        if (this.stellar$cannotUpdate(quest.getChapter())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "addProgress",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddProgressHead(Task task, long progress, CallbackInfo ci) {
        if (this.stellar$cannotUpdate(task.getQuestChapter())) {
            ci.cancel();
        }
    }

    @Unique
    @SuppressWarnings("ConstantConditions")
    private boolean stellar$cannotUpdate(Chapter chapter) {
        TeamData self = (TeamData) (Object) this;
        ChapterDependencyController controller = (ChapterDependencyController) (Object) chapter;

        return controller.stellar$isLockUntilDependenciesCompleted() && controller.stellar$isLocked(self);
    }
}
