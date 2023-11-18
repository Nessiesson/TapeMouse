package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Main mod file
 *
 * @author Dries007
 */
@SuppressWarnings({"NewExpressionSideOnly", "MethodCallSideOnly"}) // @Mod.clientSideOnly does this.
@Mod(modid = TapeMouse.MODID, name = TapeMouse.NAME, dependencies = "before:*", useMetadata = false, clientSideOnly = true)
public class TapeMouse
{
    public static final String MODID = "tapemouse";
    public static final String NAME = "TapeMouse";
    static int delay = 10;
    static KeyBinding keyBinding;
    static int i = 0;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        if (event.getSide().isClient()) ClientCommandHandler.instance.registerCommand(new CommandTapeMouse());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void textRenderEvent(RenderGameOverlayEvent.Text event)
    {
        if (keyBinding == null) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu)
        {
            event.left.add(NAME + " paused. Main menu open. If you want to AFK, use ALT+TAB.");
        }
        else if (Minecraft.getMinecraft().currentScreen instanceof GuiChat)
        {
            event.left.add(NAME + " paused. Chat GUI open. If you want to AFK, use ALT+TAB.");
        }
        else
        {
            event.left.add(NAME + " active: " + keyBinding.getKeyDescription() + " (" + keyBinding.getKeyDescription().replaceFirst("^key\\.", "") + ')');
            event.left.add("Delay: " + i + " / " + delay);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) return;
        if (keyBinding == null) return;
        if (i++ < delay) return;
        i = 0;
        if (delay == 0) KeyBinding.setKeyBindState(keyBinding.getKeyCode(), true);
        KeyBinding.onTick(keyBinding.getKeyCode());
    }
}
