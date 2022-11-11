package farzo.plugins.world.entities;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;

import com.mojang.serialization.Lifecycle;
import farzo.plugins.reflection.ObfuscatedFields;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class CustomEntity<T extends Entity, E extends Entity> {

    private final String name;
    private final Class<T> nmsClass;
    private final EntityType<T> nmsEntityType;
    private final Class<E> customClass;
    private final EntityType.Builder<E> entityBuilder;

    private final ResourceLocation minecraftKey;
    private final EntityType<E> entityType;

    public CustomEntity(String name, Class<T> nmsClass, EntityType<T> nmsEntityType, Class<E> customClass, EntityType.Builder<E> entityBuilder) {
        this.name = name;
        this.minecraftKey = new ResourceLocation(name);
        this.nmsClass = nmsClass;
        this.nmsEntityType = nmsEntityType;
        this.customClass = customClass;
        this.entityBuilder = entityBuilder;
        this.entityType = registerEntity(this.name, this.nmsEntityType, this.entityBuilder);
    }

    private static <E extends Entity> EntityType<E> registerEntity(String type, EntityType<? extends Entity> nmsEntityType, EntityType.Builder<E> entityBuilder) {
        unfreezeRegistry();

        Map<String, Type<?>> types = (Map<String, Type<?>>) DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).findChoiceType(References.ENTITY).types();
        types.put(type, types.get(Registry.ENTITY_TYPE.getKey(nmsEntityType).toString()));

        EntityType<E> customEntityType = entityBuilder.build(type);
        Optional<EntityType<?>> registeredEntity = Registry.ENTITY_TYPE.getOptional(new ResourceLocation(type));

        if(registeredEntity.isPresent()) {
            Bukkit.getLogger().log(Level.WARNING, String.format("[smart-companions] Trying to add a already registered custom entity : %s", type));
            removeIntrusiveTag(registeredEntity.get());
            removeCustomHolderProvider();
            Registry.ENTITY_TYPE.registerOrOverride(OptionalInt.of(Registry.ENTITY_TYPE.getId(registeredEntity.get())), ResourceKey.create(Registry.ENTITY_TYPE.key(), new ResourceLocation(type)), customEntityType, Lifecycle.stable());
            if(Registry.ENTITY_TYPE.getOptional(new ResourceLocation("old_" + type)).isEmpty()) Registry.registerMapping(Registry.ENTITY_TYPE, Registry.ENTITY_TYPE.getId(customEntityType), "old_" + type, registeredEntity.get());
            resetCustomHolderProvider();
        } else {
            Bukkit.getLogger().log(Level.INFO, String.format("[smart-companions] Registering entity with id %s", type));
            // EntityType<Entity> customEntityType = Registry.register(Registry.ENTITY_TYPE, type, entityBuilder.build(type));
            // CustomEntityType should be registered with the same id as the extended NMS entity in order to get the right skin model
            // We can't override getType with NmsEntityType in the custom class because NMS need the right EntityType when creating the customEntity from its builder
            Registry.registerMapping(Registry.ENTITY_TYPE, Registry.ENTITY_TYPE.getId(nmsEntityType), type, customEntityType);
        }
        return customEntityType;
    }

    private static void unfreezeRegistry() {
        Class<MappedRegistry> registryClass = MappedRegistry.class;
        try {
            Field intrusiveHolderCache = registryClass.getDeclaredField(ObfuscatedFields.INTRUSIVE_HOLDER_CACHE);
            intrusiveHolderCache.setAccessible(true);
            intrusiveHolderCache.set(Registry.ENTITY_TYPE, new IdentityHashMap<EntityType<?>, Holder.Reference<EntityType<?>>>());
            Field frozen = registryClass.getDeclaredField(ObfuscatedFields.FROZEN);
            frozen.setAccessible(true);
            frozen.set(Registry.ENTITY_TYPE, false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void removeIntrusiveTag(EntityType<?> entityType) {
        Class<EntityType> entityTypeClass = EntityType.class;
        Class<Holder.Reference> holderClass = Holder.Reference.class;
        Class<?> enumType = holderClass.getDeclaredClasses()[0];
        try {
            Field standAlone = enumType.getDeclaredField(ObfuscatedFields.HOLDER_REFERENCE_TYPE_STAND_ALONE);
            standAlone.setAccessible(true);
            Field builtInRegistryHolder = entityTypeClass.getDeclaredField(ObfuscatedFields.BUILT_IN_REGISTRY_HOLDER);
            builtInRegistryHolder.setAccessible(true);
            Field holderReferenceType = holderClass.getDeclaredField(ObfuscatedFields.HOLDER_REFERENCE_TYPE);
            holderReferenceType.setAccessible(true);
            Bukkit.getLogger().info(String.valueOf(standAlone.get(null)));
            holderReferenceType.set(builtInRegistryHolder.get(entityType), standAlone.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void removeCustomHolderProvider() {
        try {
            Field customHolderProvider = MappedRegistry.class.getDeclaredField(ObfuscatedFields.CUSTOM_HOLDER_PROVIDER);
            customHolderProvider.setAccessible(true);
            customHolderProvider.set(Registry.ENTITY_TYPE, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void resetCustomHolderProvider() {
        try {
            Field customHolderProvider = MappedRegistry.class.getDeclaredField(ObfuscatedFields.CUSTOM_HOLDER_PROVIDER);
            customHolderProvider.setAccessible(true);
            customHolderProvider.set(Registry.ENTITY_TYPE, (Function<EntityType<?>, Holder.Reference<EntityType<?>>>)EntityType::builtInRegistryHolder);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Function<EntityType<?>, Holder.Reference<EntityType<?>>> getBypassHolderProvider(ResourceLocation entityName) {
        return entityType -> Registry.ENTITY_TYPE.holders().filter(entityTypeReference -> entityTypeReference.is(entityName)).findFirst().get();
    }

    public String getName() {
        return this.name;
    }

    public EntityType<E> getEntityType() {
        return this.entityType;
    }

    public ResourceLocation getMinecraftKey() {
        return this.minecraftKey;
    }

    public EntityType<T> getNmsEntityType() {
        return nmsEntityType;
    }

    public EntityType.Builder<E> getBuilder() {
        return this.entityBuilder;
    }

    public Class<T> getNMSClass() {
        return this.nmsClass;
    }

    public Class<E> getCustomClass() {
        return this.customClass;
    }

}
