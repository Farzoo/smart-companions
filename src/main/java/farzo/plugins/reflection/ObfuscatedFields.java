package farzo.plugins.reflection;


import net.minecraft.core.Holder;

/**
 * Contains obfuscated fields
 * Should be used as a last resort e.g. register a custom entity in the register
 */
public class ObfuscatedFields {
    // net.minecraft.core.MappedRegistry#frozen
    public static final String FROZEN = "ca";
    // net.minecraft.core.MappedRegistry#intrusiveHolderCache
    public static final String INTRUSIVE_HOLDER_CACHE = "cc";
    // net.minecraft.core.MappedRegistry#customHolderProvider
    public static final String CUSTOM_HOLDER_PROVIDER = "cb";
    // net.minecraft.world.entity.EntityType#builtInRegistryHolder
    public static final String BUILT_IN_REGISTRY_HOLDER = "bq";
    // net.minecraft.core.Holder$Reference$Type type
    public static final String HOLDER_REFERENCE_TYPE = "c";
    // net.minecraft.core.Holder$Reference$Type STAND_ALONE
    public static final String HOLDER_REFERENCE_TYPE_STAND_ALONE = "a";
    // net.minecraft.core.Holder$Reference$Type INTRUSIVE
    public static final String HOLDER_REFERENCE_TYPE_INTRUSIVE = "b";

    private ObfuscatedFields() {};
}
