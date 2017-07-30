package net.doubledoordev.letitburn.letitburn;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(
        modid = LetItBurn.MOD_ID,
        name = LetItBurn.MOD_NAME,
        version = LetItBurn.VERSION,
        guiFactory = "net.doubledoordev.letitburn.letitburn.client.ConfigGuiFactory"
)
public class LetItBurn
{
    public static final Pattern PATTERN = Pattern.compile("^(?<name>.+?)(?::(?<meta>\\*|[\\d]+))? ?= ?(?<time>\\d+)$");
    public static final String MOD_ID = "letitburn";
    public static final String MOD_NAME = "LetItBurn";
    public static final String VERSION = "2.0.0";

    private static final Map<String, Integer> MAP = Maps.newHashMap();

    @Mod.Instance(MOD_ID)
    public static LetItBurn INSTANCE;

    private Logger logger;
    private Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        updateConfig();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void updateConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MOD_ID)) updateConfig();
    }

    private void updateConfig()
    {
        logger.info("Update config");

        MAP.clear();
        for (String l : config.get(Configuration.CATEGORY_GENERAL, "burnTimes", new String[] {"minecraft:vine = 100"}, "", PATTERN).getStringList())
        {
            Matcher m = PATTERN.matcher(l);
            if (!m.find())
            {
                logger.warn("Config item '{}' does not match valid pattern ('{}'). It cannot be parsed.", l, PATTERN);
                continue;
            }

            ResourceLocation rl = new ResourceLocation(m.group("name"));
            int metaInt = OreDictionary.WILDCARD_VALUE;
            String meta = m.group("meta");
            if (meta != null && !meta.equals("*"))
            {
                metaInt = Integer.parseInt(meta);
            }
            MAP.put(makeKey(rl,metaInt ), Integer.parseInt(m.group("time")));
        }

        if (config.hasChanged()) config.save();
    }

    private static String makeKey(ResourceLocation rl, int meta)
    {
        return rl + "\t" + meta;
    }

    @SubscribeEvent
    public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event)
    {
        String k = makeKey(event.getItemStack().getItem().getRegistryName(), event.getItemStack().getMetadata());
        if (MAP.containsKey(k))
        {
            event.setBurnTime(MAP.get(k));
            return;
        }
        k = makeKey(event.getItemStack().getItem().getRegistryName(), OreDictionary.WILDCARD_VALUE);
        if (MAP.containsKey(k)) event.setBurnTime(MAP.get(k));
    }

    public static Configuration getConfig()
    {
        return INSTANCE.config;
    }
}
