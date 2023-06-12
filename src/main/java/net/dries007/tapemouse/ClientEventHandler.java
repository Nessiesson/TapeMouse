package net.dries007.tapemouse;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.dries007.tapemouse.TapeMouse.LOGGER;
import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;

/**
 * Client side only code
 * @author Dries007
 */
public class ClientEventHandler
{
    private static final Map<String, KeyMapping> KEYBIND_ARRAY = ObfuscationReflectionHelper.getPrivateValue(KeyMapping.class, null, "f_90809_");
    private final Minecraft mc;

    private int delay;
    private KeyMapping key;
    private int i;

    public ClientEventHandler()
    {
        this.mc = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        if (KEYBIND_ARRAY == null)
        {
            RuntimeException e = new NullPointerException("KEYBIND_ARRAY was null.");
            LOGGER.fatal("Something has gone wrong fetching the KeyMapping list. I guess we die now.", e);
            throw e;
        }
    }

    /**
     * Draw info on the screen
     */
    @SubscribeEvent
    public void textRenderEvent(CustomizeGuiOverlayEvent.DebugText event)
    {
        if (key == null) return;
        if (mc.screen instanceof TitleScreen || mc.screen instanceof ChatScreen)
        {
            event.getLeft().add("TapeMouse paused. If you want to AFK, use ALT+TAB.");
            return;
        }
        event.getLeft().add("TapeMouse active: " + key.getTranslatedKeyMessage().getString() + " (" + key.getName().replaceFirst("^key\\.", "") + ')');
        event.getLeft().add("Delay: " + i + " / " + delay);
    }

    /**
     * Actually trigger the KeyMapping.
     */
    @SubscribeEvent(priority = HIGHEST)
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.screen instanceof TitleScreen || mc.screen instanceof ChatScreen) return;
        if (key == null) return;
        if (i++ < delay) return;
        i = 0;
        if (delay == 0) key.setDown(true);
        KeyMapping.click(key.getKey());
    }

    /**
     * DIY Client side command
     */
    @SubscribeEvent(priority = HIGHEST)
    public void chatEvent(ClientChatEvent event)
    {
        if (!event.getMessage().startsWith("!tapemouse")) return;
        LOGGER.info("{}", event.getMessage());
        event.setCanceled(true);
        String[] args = event.getOriginalMessage().split("\\s");
        ChatComponent gui = mc.gui.getChat();
        gui.addRecentChat(event.getOriginalMessage());
        try
        {
            handleCommand(gui, args);
        }
        catch (Exception e)
        {
            gui.addMessage(Component.literal("An error occurred trying to run the tapemouse command:").withStyle(ChatFormatting.RED));
            gui.addMessage(Component.literal(e.toString()).withStyle(ChatFormatting.RED));
            LOGGER.error("An error occurred trying to run the tapemouse command:", e);
        }
    }

    private void handleCommand(ChatComponent gui, String[] args) throws Exception
    {
        switch (args.length)
        {
            default:
                gui.addMessage(Component.literal("TapeMouse help: ").withStyle(ChatFormatting.AQUA));
                gui.addMessage(Component.literal("Run '!tapemouse list' to get a list of keybindings."));
                gui.addMessage(Component.literal("Run '!tapemouse off' to stop TapeMouse."));
                gui.addMessage(Component.literal("Run '!tapemouse <binding> <delay>' to start TapeMouse."));
                gui.addMessage(Component.literal("  delay is the number of ticks between every keypress. Set to 0 to hold down the key."));
                return;
            case 2:
                if (args[1].equalsIgnoreCase("off"))
                {
                    this.key = null;
                    return;
                }
                else if (args[1].equalsIgnoreCase("list"))
                {
                    List<String> keys = KEYBIND_ARRAY.keySet().stream().map(k -> k.replaceFirst("^key\\.", "")).sorted().collect(Collectors.toList());
                    gui.addMessage(Component.literal(String.join(", ", keys)));
                }
                else
                {
                    gui.addMessage(Component.literal("Missing delay parameter.").withStyle(ChatFormatting.RED));
                }
                break;
            case 3:
            {
                KeyMapping KeyMapping = KEYBIND_ARRAY.get("key." + args[1]);
                if (KeyMapping == null)
                {
                    KeyMapping = KEYBIND_ARRAY.get(args[1]);
                }
                if (KeyMapping == null)
                {
                    gui.addMessage(Component.literal(args[1] + " is not a valid KeyMapping.").withStyle(ChatFormatting.RED));
                    return;
                }
                int delay;
                try
                {
                    delay = Integer.parseInt(args[2]);
                     if (delay < 0) throw new Exception("bad user");
                }
                catch (Exception e)
                {
                    gui.addMessage(Component.literal(args[1] + " is not a positive number or 0.").withStyle(ChatFormatting.RED));
                    return;
                }

                this.delay = delay;
                this.i = 0;
                this.key = KeyMapping;
            }
            break;
        }
    }
}
