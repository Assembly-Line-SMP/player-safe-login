package io.SousaLJ.playersafelogin.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PlayerSafeLoginConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SERVER = new ServerConfig(builder);
        SERVER_SPEC = builder.build();
    }

    public static class ServerConfig {
        public final ModConfigSpec.EnumValue<StorageType> storageType;
        public final ModConfigSpec.ConfigValue<String> mysqlHost;
        public final ModConfigSpec.IntValue mysqlPort;
        public final ModConfigSpec.ConfigValue<String> mysqlUser;
        public final ModConfigSpec.ConfigValue<String> mysqlPassword;
        public final ModConfigSpec.ConfigValue<String> mysqlDbName;
        public final ModConfigSpec.ConfigValue<String> sqlitePath;

        public ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Server Config").push("server");

            storageType = builder
                    .comment("Storage type")
                    .defineEnum("storageType", StorageType.FILE);

            builder.comment("MySQL config").push("mysql");
            mysqlHost = builder.define("host", "localhost");
            mysqlPort = builder.defineInRange("port", 3306, 1, 65535);
            mysqlUser = builder.define("user", "root");
            mysqlPassword = builder.define("password", "");
            mysqlDbName = builder.define("database", "safelogin");
            builder.pop();

            builder.comment("SQLite config").push("sqlite");
            sqlitePath = builder.define("path", "./safelogin/safelogin.db");
            builder.pop();

            builder.pop();
        }
    }
}