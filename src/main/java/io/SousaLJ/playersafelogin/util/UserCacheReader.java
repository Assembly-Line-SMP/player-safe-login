package io.SousaLJ.playersafelogin.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.SousaLJ.playersafelogin.PlayerSafeLogin;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class UserCacheReader {
    private static final Gson GSON = new Gson();
    private static final Path USER_CACHE_PATH = Paths.get("usercache.json");

    public static List<String> getCachedPlayerNames() {
        try (Reader reader = Files.newBufferedReader(USER_CACHE_PATH)) {
            List<CachedUser> users = GSON.fromJson(reader,
                    new TypeToken<List<CachedUser>>(){}.getType());

            return users.stream()
                    .map(CachedUser::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            PlayerSafeLogin.LOGGER.error("Failed to read usercache.json", e);
            return List.of();
        }
    }
}
