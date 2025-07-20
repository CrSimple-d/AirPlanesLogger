package net.crsimple.airplaneslogger;

import com.mojang.logging.LogUtils;
import net.crsimple.airplaneslogger.commands.OwnerCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(AirplanesLogger.MOD_ID)
public class AirplanesLogger {
    public static final String MOD_ID = "airplanes_logger";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String LOG_PATTERN = "Игрок: {}, {} самолет({}) с именем: {}, X: {} Y: {} Z: {}. {}";

    public AirplanesLogger(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing {}",MOD_ID);
    }

    public static void log(String action, String id, Component name, Player user, @Nullable String owner) {
        var pos = user.getOnPos();
        if(user.getDisplayName() != null && name != null) {
            AirplanesLogger.LOGGER.info(AirplanesLogger.LOG_PATTERN, user.getDisplayName().getString(), action, id, name.getString(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    owner==null||owner.isEmpty() ?"":"Владелец самолета: " + owner
            );
        }
    }
    @EventBusSubscriber(modid = AirplanesLogger.MOD_ID,value = {Dist.CLIENT,Dist.DEDICATED_SERVER})
    static class ModCommands {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent e) {
            OwnerCommand.reg(e.getDispatcher());
        }
    }
}
