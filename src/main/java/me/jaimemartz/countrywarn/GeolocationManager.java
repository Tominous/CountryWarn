package me.jaimemartz.countrywarn;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

public class GeolocationManager {
    private final DatabaseReader reader;

    public GeolocationManager(CountryWarn plugin) throws IOException {
        File dir = new File(plugin.getDataFolder(), "database");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File packed = new File(dir, "GeoLite2-Country.mmdb.gz");
        File database = new File(dir, "GeoLite2-Country.mmdb");

        if (!database.exists()) {
            plugin.getLogger().info("Downloading database");
            URL url = new URL("http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.mmdb.gz");
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream())) {
                try (FileOutputStream fos = new FileOutputStream(packed)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            }

            plugin.getLogger().info("Unpacking database");
            byte[] buffer = new byte[1024];
            try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(packed))) {
                try (FileOutputStream out = new FileOutputStream(database)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }

            plugin.getLogger().info("Deleting packed archive, success: " + (packed.delete() ? "yes" : "no"));
        } else {
            plugin.getLogger().info("Database exists, no need to download again");
        }

        plugin.getLogger().info("Initializing database");
        reader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
    }

    public DatabaseReader getReader() {
        return reader;
    }
}