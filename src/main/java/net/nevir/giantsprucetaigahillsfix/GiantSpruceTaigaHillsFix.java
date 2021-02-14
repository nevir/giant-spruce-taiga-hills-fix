package net.nevir.giantsprucetaigahillsfix;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("giantsprucetaigahillsfix")
public class GiantSpruceTaigaHillsFix
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final  ResourceLocation GIANT_SPRUCE_TAIGA_HILLS_ID = new ResourceLocation("giant_spruce_taiga_hills");

    public GiantSpruceTaigaHillsFix() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::patchGiantSpruceTaigaHills);
    }

    /**
     * Monkey patch the giant spruce taiga hills biome to fix
     * https://bugs.mojang.com/browse/MC-140690
     */
    private void patchGiantSpruceTaigaHills(final FMLCommonSetupEvent event) {
        Biome giantSpruceTaigaHills = ForgeRegistries.BIOMES.getValue(GIANT_SPRUCE_TAIGA_HILLS_ID);
        if (giantSpruceTaigaHills == null) {
            LOGGER.error("The biome {} doesn't appear to exist. Skipping fix", GIANT_SPRUCE_TAIGA_HILLS_ID);
            return;
        }

        ArrayList<Field> targetFields = new ArrayList<Field>();

        // Thankfully, as of 1.16.5 there are only two float properties on
        // the biome: baseHeight and heightVariation.
        //
        // And, per the bug, they are both set to 0.2 rather than their
        // hilly values.
        //
        // So we find those fields, and assume that they are declared in that
        // order.
        for (Field field : Biome.class.getDeclaredFields()) {

            if (field.getType() != Float.TYPE) continue;

            field.setAccessible(true);
            try {
                if (field.getFloat(giantSpruceTaigaHills) == 0.2f) {
                    targetFields.add(field);
                }
            } catch (IllegalAccessException exception) {
                LOGGER.error("Unexpected error when accessing private field: {}", exception);
                continue;
            }
        }

        // We assume the fields are defined in order baseHeight and heightVariation.
        if (targetFields.size() != 2) {
            LOGGER.error("Expected exactly 2 fields equalling 0.2f on the biome. Found {} instead. Skipping fix", targetFields.size());
            return;
        }

        try {
            targetFields.get(0).setFloat(giantSpruceTaigaHills, 0.45f);
            targetFields.get(1).setFloat(giantSpruceTaigaHills, 0.3f);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
