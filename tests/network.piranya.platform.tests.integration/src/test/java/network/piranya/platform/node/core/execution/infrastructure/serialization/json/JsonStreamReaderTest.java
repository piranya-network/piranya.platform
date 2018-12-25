package network.piranya.platform.node.core.execution.infrastructure.serialization.json;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JsonStreamReaderTest {

	@Test
	public void testStreamSerialization() {
		JsonStreamReaderImpl jsonStreamReader = new JsonStreamReaderImpl(getClass().getResourceAsStream("BITFINEX_SPOT_BTC_USD-ob-0420.txt"));
		List<OrderBookEntry> entries = new ArrayList<>();
		jsonStreamReader.readArray(OrderBookEntry.class, o -> entries.add(o));
		assertEquals(100, entries.size());
		assertEquals("BITFINEX_SPOT_BTC_USD", entries.get(0).getSymbol_id());
		assertEquals(20, entries.get(0).getBids().size());
	}

}
