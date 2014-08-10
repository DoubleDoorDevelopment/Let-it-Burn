/*
 * Copyright (c) 2014, DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.letitburn;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.doubledoordev.lib.DevPerks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dries007
 */
@Mod(modid = LetItBurn.MODID)
public class LetItBurn
{
    public static final String MODID = "LetItBurn";
    public static final Pattern PATTERN = Pattern.compile("(?<name>.+?):?(?<meta>[\\d]+)? ?= ?(?<time>\\d+)");

    @Mod.Instance(MODID)
    public static LetItBurn instance;

    private String[]                      strings     = new String[] {"minecraft:vine = 100"};
    private boolean                       debug       = false;
    private HashMap<String, BurnTimeData> burnTimeMap = new HashMap<>();
    private Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        String comment = "List formatted like this:  'itemname = time' OR 'itemname;meta = time'  Include the modid (or minecraft:)!";
        // public Property get(String category, String key, String[] defaultValues, String comment, Pattern validationPattern)
        strings = configuration.get(MODID, "burntimes", strings, comment, PATTERN).getStringList();

        debug = configuration.getBoolean("debug", MODID, debug, "Enable extra debug output.");
        if (configuration.getBoolean("sillyness", MODID, true, "Disable sillyness only if you want to piss off the developers XD")) MinecraftForge.EVENT_BUS.register(new DevPerks(debug));
        if (configuration.hasChanged()) configuration.save();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        for (String string : strings)
        {
            if (debug) logger.info(string);
            Matcher matcher = PATTERN.matcher(string);
            if (!matcher.find()) continue;

            String name = matcher.group("name");
            int meta = -1;
            try
            {
                meta = Integer.parseInt(matcher.group("meta"));
            }
            catch (Exception e)
            {
                // We don't care
            }
            int time = Integer.parseInt(matcher.group("time"));
            if (!burnTimeMap.containsKey(name)) burnTimeMap.put(name, new BurnTimeData(name));

            if (debug) logger.info(matcher);
            burnTimeMap.get(name).addData(meta, time);
        }

        GameRegistry.registerFuelHandler(new IFuelHandler()
        {
            @Override
            public int getBurnTime(ItemStack fuel)
            {
                String name = GameData.getItemRegistry().getNameForObject(fuel.getItem());
                if (instance.burnTimeMap.containsKey(name))
                {
                    if (instance.debug) logger.info("Custom fuel " + name);
                    return instance.burnTimeMap.get(name).getTime(fuel.getItemDamage());
                }
                return 0;
            }
        });
    }

    public static final class BurnTimeData
    {
        public final String name;

        int                       wildcardTime = 0;
        HashMap<Integer, Integer> data         = new HashMap<>();

        public BurnTimeData(String name)
        {
            this.name = name;
            if (instance.debug) instance.logger.info("New BurnTimeData: " + name);
        }

        public int getTime(int itemDamage)
        {
            return data.containsKey(itemDamage) ? data.get(itemDamage) : wildcardTime;
        }

        public void addData(int meta, int time)
        {
            if (instance.debug) instance.logger.info("Add burntime to " + name + ": Meta=" + meta + " Time=" + time);
            if (meta == -1) wildcardTime = time;
            else data.put(meta, time);
        }
    }
}
