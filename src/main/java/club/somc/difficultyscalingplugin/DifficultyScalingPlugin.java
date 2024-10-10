package club.somc.difficultyscalingplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class DifficultyScalingPlugin extends JavaPlugin implements Listener {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            Location spawnLocation = event.getLocation();
            World world = spawnLocation.getWorld();

            if (world == null || world.getSpawnLocation() == null) {
                return;
            }

            double distanceFromSpawn = spawnLocation.distance(world.getSpawnLocation());
            LivingEntity entity = event.getEntity();

            applyDifficultyScaling(entity, distanceFromSpawn);
        }
    }

    private void applyDifficultyScaling(LivingEntity entity, double distance) {
        float level = (float)distance / 500.0f;

        applyArmorUpgrade(entity, level);
        applyWeaponUpgrade(entity, level);
        applySpecialEffects(entity, level);
        convertToStrongerMob(entity, level);
    }

    private void applyArmorUpgrade(LivingEntity entity, float level) {

        // 0 to 1 is nothing
        // 1 - 6 are amour upgrades
        // 7 adds totems
        // nothing above, just random select from below.

        if (level < 1) return;

        boolean totems = false;

        Material[] helmets = {
                Material.LEATHER_HELMET,
                Material.CHAINMAIL_HELMET,
                Material.GOLDEN_HELMET,
                Material.IRON_HELMET,
                Material.DIAMOND_HELMET,
                Material.NETHERITE_HELMET
        };

        Material[] chestplates = {
                Material.LEATHER_CHESTPLATE,
                Material.CHAINMAIL_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE,
                Material.IRON_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE,
                Material.NETHERITE_CHESTPLATE
        };

        Material[] legs = {
                Material.LEATHER_LEGGINGS,
                Material.CHAINMAIL_LEGGINGS,
                Material.GOLDEN_LEGGINGS,
                Material.IRON_LEGGINGS,
                Material.DIAMOND_LEGGINGS,
                Material.NETHERITE_LEGGINGS
        };

        Material[] boots = {
                Material.LEATHER_BOOTS,
                Material.CHAINMAIL_BOOTS,
                Material.GOLDEN_BOOTS,
                Material.IRON_BOOTS,
                Material.DIAMOND_BOOTS,
                Material.NETHERITE_BOOTS
        };

        int gear_level = (int)level;
        if (gear_level > boots.length) {
            totems = true;
            gear_level = boots.length;
        }

        if (totems)
            entity.getEquipment().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));

        // 25% chance of chestplate (25%, 50%, 75%, 100%)
        if (random.nextFloat() < 0.25f * level) {
            entity.getEquipment().setChestplate(new ItemStack(chestplates[random.nextInt(gear_level)]));
            entity.getEquipment().setChestplateDropChance(0.20f / level); // scale down as you get harder. 20%, 10%, 5%,...
        }

        // 10% chance of legs (10%, 20%, 30%, ...)
        if (random.nextFloat() < 0.10f * level) {
            entity.getEquipment().setLeggings(new ItemStack(legs[random.nextInt(gear_level)]));
            entity.getEquipment().setLeggingsDropChance(0.20f / level);
        }

        // 5% chance of boots (5%, 10%, 15%, ...)
        if (random.nextFloat() < 0.05f * level) {
            entity.getEquipment().setBoots(new ItemStack(boots[random.nextInt(gear_level)]));
            entity.getEquipment().setBootsDropChance(0.20f / level);
        }

        // 3% chance of helmet (3%, 6%, 9%, ...)
        if (random.nextFloat() < 0.03f * level) {
            entity.getEquipment().setHelmet(new ItemStack(helmets[random.nextInt(gear_level)]));
            entity.getEquipment().setHelmetDropChance(0.20f / level);
        }

    }

    private void applyWeaponUpgrade(LivingEntity entity, float level) {

        // if level < 3, no weapon.

        Material[] swords = {
                Material.WOODEN_SWORD,
                Material.GOLDEN_SWORD,
                Material.IRON_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD
        };

        int gear_level = (int)level - 3;
        if (gear_level > swords.length) {
            gear_level = swords.length;
        }

        // 10% chance of sword (10%, 20%, 30%, ...)
        if (entity instanceof Zombie && random.nextFloat() < 0.10f * (level-3.0f)) {
            entity.getEquipment().setItemInMainHand(new ItemStack(swords[random.nextInt(gear_level)]));
            entity.getEquipment().setItemInMainHandDropChance(0.20f / (level-3.0f));
        }
    }

    private void applySpecialEffects(LivingEntity entity, float level) {

        // level less than 4, do nothing
        if (level < 4) return;

        // 10% chance of sword (10%, 20%, 30%, ...)
        if (entity instanceof Creeper && random.nextFloat() < 0.10f * (level-4.0f)) {
            ((Creeper) entity).setPowered(true);
        }
    }


    private void convertToStrongerMob(LivingEntity entity, float level) {

        if (!(entity instanceof Monster)) return;

        // if level < 6 do nothing.
        if (level < 6) return;

        Location location = entity.getLocation();
        World world = location.getWorld();

        if (world == null) {
            return;
        }

        if (random.nextFloat() < 0.0025 * (level-6.0f)) { // 0.25% chance for Wither
            entity.remove();
            world.spawnEntity(location, EntityType.WITHER);
        } else if (random.nextFloat() < 0.02 * (level-6.0f)) { // 2% chance for Warden
            entity.remove();
            world.spawnEntity(location, EntityType.WARDEN);
        } else if (random.nextFloat() < 0.05 * (level-6.0f)) { // 5% chance for other
            EntityType[] strongMobs = {EntityType.RAVAGER, EntityType.EVOKER, EntityType.VINDICATOR};
            entity.remove();
            world.spawnEntity(location, strongMobs[random.nextInt(strongMobs.length)]);
        }
    }
}
