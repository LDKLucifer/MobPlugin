package nukkitcoders.mobplugin.entities.monster.flying;

import cn.nukkit.Player;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import nukkitcoders.mobplugin.entities.Boss;
import nukkitcoders.mobplugin.entities.monster.FlyingMonster;
import nukkitcoders.mobplugin.entities.projectile.EntityBlueWitherSkull;
import nukkitcoders.mobplugin.utils.Utils;

public class Wither extends FlyingMonster implements Boss, EntitySmite {

    public static final int NETWORK_ID = 52;

    public Wither(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 3.5f;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(600);
        this.setHealth(600);
        this.setDamage(new float[]{0, 2, 4, 6});
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.isSurvival() || player.isAdventure()) && distance <= 200;
        }
        return creature.isAlive() && !creature.closed && distance <= 200;
    }

    @Override
    public int getKillExperience() {
        return 50;
    }

    @Override
    public void attackEntity(Entity player) {
    if (this.attackDelay > 23 && Utils.rand(1, 5) < 3 && this.distance(player) <= 100) {
            this.attackDelay = 0;

            double f = 1;
            double yaw = this.yaw + Utils.rand(-12.0, 12.0);
            double pitch = this.pitch + Utils.rand(-7.0, 7.0);
            Location pos = new Location(this.x - Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, this.y + this.getEyeHeight(),
                    this.z + Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, yaw, pitch, this.level);
            Entity k = Entity.createEntity("BlueWitherSkull", pos, this);
            if (!(k instanceof EntityBlueWitherSkull)) {
                return;
            }

            EntityBlueWitherSkull blueskull = (EntityBlueWitherSkull) k;
            blueskull.setExplode(true);
            blueskull.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));

            ProjectileLaunchEvent launch = new ProjectileLaunchEvent(blueskull);
            this.server.getPluginManager().callEvent(launch);
            if (launch.isCancelled()) {
                blueskull.close();
            } else {
                blueskull.spawnToAll();
                this.level.addSound(this, Sound.MOB_WITHER_SHOOT);
            }
        }
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.NETHER_STAR, 0, 1)};
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket addEntity = new AddEntityPacket();
        addEntity.type = this.getNetworkId();
        addEntity.entityUniqueId = this.getId();
        addEntity.entityRuntimeId = this.getId();
        addEntity.yaw = (float) this.yaw;
        addEntity.headYaw = (float) this.yaw;
        addEntity.pitch = (float) this.pitch;
        addEntity.x = (float) this.x;
        addEntity.y = (float) this.y;
        addEntity.z = (float) this.z;
        addEntity.speedX = (float) this.motionX;
        addEntity.speedY = (float) this.motionY;
        addEntity.speedZ = (float) this.motionZ;
        addEntity.metadata = this.dataProperties;
        addEntity.attributes = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(200).setValue(200)};
        return addEntity;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        return super.entityBaseTick(tickDiff);
    }

    @Override
    public int nearbyDistanceMultiplier() {
        return 30;
    }
}
