package com.luolian.stellarmod.api.compat;

import com.luolian.stellarmod.api.util.OriginsUtil;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.common.capabilities.OriginContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.concurrent.atomic.AtomicBoolean;

public class OriginsTask extends AbstractBooleanTask {
    private ResourceLocation originLayer = OriginsUtil.originsLocation("origin");
    private ResourceLocation origin = OriginsUtil.originsLocation("human");

    public OriginsTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer serverPlayer) {
        AtomicBoolean rt = new AtomicBoolean(false);

        ResourceKey<OriginLayer> originLayerKey = OriginsUtil.getOriginLayerKey(this.originLayer);
        ResourceKey<Origin> originKey = OriginsUtil.getOriginKey(this.origin);
        LazyOptional<IOriginContainer> capability = serverPlayer.getCapability(OriginsAPI.ORIGIN_CONTAINER);

        capability.ifPresent(container -> {
            OriginContainer originContainer = (OriginContainer) container;
            // 检查指定layer中是否存在指定origin
            if (originContainer.getOrigin(originLayerKey).equals(originKey)) {
                rt.set(true);
            }
        });
        return rt.get();
    }

    @Override
    public TaskType getType() {
        return StellarTaskTypes.ORIGIN;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);

        config.addString(
                "origin_layer",
                originLayer.toString(),
                v -> originLayer = ResourceLocation.tryParse(v),
                "origins:origin"
        ).setIcon(OriginsUtil.getOriginIcon())
                .setNameKey("ftbquests.task.stellarmod.origin_layer");
        config.addString(
                "origin",
                origin.toString(),
                v -> origin = ResourceLocation.tryParse(v),
                "origin:human"
        ).setIcon(OriginsUtil.getOriginIcon())
                .setNameKey("ftbquests.task.stellarmod.origin");
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putString("origin_layer", originLayer.toString());
        nbt.putString("origin", origin.toString());
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        this.originLayer = ResourceLocation.tryParse(nbt.getString("origin_layer"));
        this.origin = ResourceLocation.tryParse(nbt.getString("origin"));
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(originLayer.toString());
        buffer.writeUtf(origin.toString());
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.originLayer = ResourceLocation.tryParse(buffer.readUtf());
        this.origin = ResourceLocation.tryParse(buffer.readUtf());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getAltTitle() {
        if (OriginsUtil.isValidOrigin(this.origin)) {
            return Component.translatable("ftbquests.task.stellarmod.origin").append(": ").append(OriginsUtil.getOriginName(this.origin)).withStyle(ChatFormatting.YELLOW);
        }
        return super.getAltTitle();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        if (OriginsUtil.isValidOrigin(this.origin)) {
            return OriginsUtil.getOriginIcon(this.origin);
        }
        return super.getAltIcon();
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 20;
    }
}
