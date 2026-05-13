package game.protocol;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import config.Config;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProtocolVersionHandlerTest {

    @Test
    void bestMatch() {
        ProtocolVersionHandler pvh = ProtocolVersionHandler.getInstance();

        Map<Integer, String> versions = new HashMap<>();
        versions.put(340, "1.12.2");
        versions.put(404, "1.13.2");
        versions.put(498, "1.14.4");
        versions.put(578, "1.15.2");
        versions.put(754, "1.16.2");
        versions.put(755, "1.17");
        versions.put(756, "1.17");
        versions.put(757, "1.18");
        versions.put(758, "1.18");
        versions.put(761, "1.19.3");
        versions.put(763, "1.20");
        versions.put(764, "1.20.2");
        versions.put(767, "1.21");
        versions.put(775, "26.1.2");

        versions.forEach((k, v) -> {
            assertThat(pvh.getProtocolByProtocolVersion(k).getVersion()).isEqualTo(v);
        });
    }

    /**
     * Verifies the packet IDs that the downloader actively parses or rewrites for the latest
     * Minecraft Java Edition release. The expected values were checked against the vanilla
     * 26.1.2 server jar's GameProtocols registration order and cover the chunk, light,
     * dimension, container, entity, render-distance, and configuration-transition packets
     * used by the proxy.
     */
    @Test
    void latestReleasePacketIds() {
        Protocol protocol = ProtocolVersionHandler.getInstance().getProtocolByProtocolVersion(775);

        assertThat(protocol.getVersion()).isEqualTo("26.1.2");
        assertThat(protocol.getDataVersion()).isEqualTo(4790);

        assertThat(protocol.get(0x2C, true)).isEqualTo("LevelChunkWithLight");
        assertThat(protocol.get(0x2F, true)).isEqualTo("LightUpdate");
        assertThat(protocol.get(0x30, true)).isEqualTo("Login");
        assertThat(protocol.get(0x50, true)).isEqualTo("Respawn");
        assertThat(protocol.get(0x52, true)).isEqualTo("SectionBlocksUpdate");
        assertThat(protocol.get(0x5D, true)).isEqualTo("SetChunkCacheRadius");
        assertThat(protocol.get(0x74, true)).isEqualTo("StartConfiguration");
        assertThat(protocol.clientBound("LevelChunkWithLight")).isEqualTo(0x2C);
        assertThat(protocol.clientBound("SetChunkCacheRadius")).isEqualTo(0x5D);

        assertThat(protocol.get(0x10, false)).isEqualTo("ConfigurationAcknowledged");
        assertThat(protocol.get(0x1D, false)).isEqualTo("MovePlayerPos");
        assertThat(protocol.get(0x34, false)).isEqualTo("SetCommandBlock");
        assertThat(protocol.get(0x40, false)).isEqualTo("UseItemOn");
        assertThat(protocol.get(0x41, false)).isEqualTo("UseItem");
    }

    /**
     * Confirms configuration-state packet IDs for 26.1.2, where registry-data moved from
     * the 1.20.6/1.21 ID 0x07 to 0x06 while finish-configuration remains serverbound 0x02.
     */
    @Test
    void latestReleaseConfigurationPacketIds() {
        Config.setInstance(new Config());
        Config.setProtocolVersion(775);

        ConfigurationProtocol protocol = new ConfigurationProtocol();

        assertThat(protocol.get(0x06, true)).isEqualTo("RegistryData");
        assertThat(protocol.get(0x07, true)).isEqualTo("UNKNOWN");
        assertThat(protocol.get(0x02, false)).isEqualTo("FinishConfiguration");
    }
}