package com.luolian.stellarmod.server.listener;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import com.luolian.stellarmod.server.worldgen.dimension.StellarDimensions;
import io.github.edwinmindcraft.calio.api.event.CalioDynamicRegistryEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = StellarMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventListener {
    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        OriginsUtil.buildOriginToLayerCache();
        OriginsUtil.handleOriginChange(event.getPlayer());
    }

    @SubscribeEvent
    public static void reloadComplete(CalioDynamicRegistryEvent.LoadComplete event) {
        OriginsUtil.buildOriginToLayerCache();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            OriginsUtil.handleOriginChange(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(StellarDimensions.STELLAR_SPACE_LEVEL_KEY)) return;

        StructureSpawnData data = level.getDataStorage().computeIfAbsent(
                StructureSpawnData::load,
                StructureSpawnData::new,
                StellarMod.MOD_ID + "_kjz_spawned"
        );

        if (!data.hasSpawned) {
            StructureTemplateManager templateManager = level.getStructureManager();
            Optional<StructureTemplate> templateOpt = templateManager.get(StellarMod.location("kjz"));

            if (templateOpt.isPresent()) {
                StructureTemplate template = templateOpt.get();
                BlockPos pos = new BlockPos(0, 60, 0);
                StructurePlaceSettings settings = new StructurePlaceSettings();
                template.placeInWorld(level, pos, pos, settings, level.getRandom(), 2);

                data.hasSpawned = true;
                data.setDirty();
            }
        }
    }

    public static class StructureSpawnData extends SavedData {
        public boolean hasSpawned = false;

        public StructureSpawnData() {}

        public static StructureSpawnData load(CompoundTag tag) {
            StructureSpawnData data = new StructureSpawnData();
            data.hasSpawned = tag.getBoolean("hasSpawned");
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean("hasSpawned", hasSpawned);
            return tag;
        }
    }
}
