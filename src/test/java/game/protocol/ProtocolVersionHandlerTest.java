package game.protocol;

import static java.util.Map.entry;
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

        Map.ofEntries(
            entry(0x01, "AddEntity"),
            entry(0x06, "BlockEntityData"),
            entry(0x08, "BlockUpdate"),
            entry(0x11, "ContainerClose"),
            entry(0x12, "ContainerSetContent"),
            entry(0x18, "CustomPayload"),
            entry(0x25, "ForgetLevelChunk"),
            entry(0x2D, "LevelChunkWithLight"),
            entry(0x30, "LightUpdate"),
            entry(0x31, "Login"),
            entry(0x33, "MapItemData"),
            entry(0x34, "MerchantOffers"),
            entry(0x35, "MoveEntityPos"),
            entry(0x36, "MoveEntityPosRot"),
            entry(0x3A, "OpenScreen"),
            entry(0x45, "PlayerInfoUpdate"),
            entry(0x4C, "RemoveEntities"),
            entry(0x51, "Respawn"),
            entry(0x53, "SectionBlocksUpdate"),
            entry(0x56, "SetActionBarText"),
            entry(0x5E, "SetChunkCacheRadius"),
            entry(0x62, "SetEntityData"),
            entry(0x65, "SetEquipment"),
            entry(0x75, "StartConfiguration"),
            entry(0x78, "SystemChat"),
            entry(0x7C, "TeleportEntity")
        ).forEach((id, name) -> assertThat(protocol.get(id, true)).isEqualTo(name));

        assertThat(protocol.clientBound("LevelChunkWithLight")).isEqualTo(0x2D);
        assertThat(protocol.clientBound("LightUpdate")).isEqualTo(0x30);
        assertThat(protocol.clientBound("Login")).isEqualTo(0x31);
        assertThat(protocol.clientBound("SetChunkCacheRadius")).isEqualTo(0x5E);

        Map.ofEntries(
            entry(0x10, "ConfigurationAcknowledged"),
            entry(0x13, "ContainerClose"),
            entry(0x16, "CustomPayload"),
            entry(0x1A, "Interact"),
            entry(0x1E, "MovePlayerPos"),
            entry(0x1F, "MovePlayerPosRot"),
            entry(0x20, "MovePlayerRot"),
            entry(0x22, "MoveVehicle"),
            entry(0x35, "SetCommandBlock"),
            entry(0x41, "UseItemOn"),
            entry(0x42, "UseItem")
        ).forEach((id, name) -> assertThat(protocol.get(id, false)).isEqualTo(name));
    }

    /**
     * Confirms configuration-state packet IDs for 26.1.2. Cookie/common configuration packets are
     * part of the vanilla registration order, so RegistryData remains clientbound 0x07 and
     * FinishConfiguration remains serverbound 0x03.
     */
    @Test
    void latestReleaseConfigurationPacketIds() {
        Config.setInstance(new Config());
        Config.setProtocolVersion(775);

        ConfigurationProtocol protocol = new ConfigurationProtocol();

        assertThat(protocol.get(0x07, true)).isEqualTo("RegistryData");
        assertThat(protocol.get(0x06, true)).isEqualTo("UNKNOWN");
        assertThat(protocol.get(0x03, false)).isEqualTo("FinishConfiguration");
    }
}