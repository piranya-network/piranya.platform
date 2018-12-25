package network.piranya.platform.api.utilities;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class PnlCalculatorTests {

	@Test
	public void testAddSame() {
		PnlCalculator pnl = new PnlCalculator();
		pnl.acceptFill(new BigDecimal("1.2"), new BigDecimal("0.1"));
		assertEquals(new BigDecimal("1.2"), pnl.openPrice().setScale(1,  RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("0.1"), pnl.openSize());
		pnl.acceptFill(new BigDecimal("0.6"), new BigDecimal("0.05"));
		assertEquals(new BigDecimal("1.0"), pnl.openPrice().setScale(1,  RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("0.15"), pnl.openSize());
	}

	@Test
	public void testFill() {
		PnlCalculator pnl = new PnlCalculator();
		pnl.acceptFill(new BigDecimal("1.2"), new BigDecimal("0.1"));
		assertEquals(new BigDecimal("1.2"), pnl.openPrice().setScale(1,  RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("0.1"), pnl.openSize());
		
		pnl.acceptFill(new BigDecimal("0.9"), new BigDecimal("-0.04"));
		assertEquals(new BigDecimal("1.2"), pnl.openPrice().setScale(1,  RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("0.06"), pnl.openSize());
		assertEquals(new BigDecimal("0.04"), pnl.realizedSize());
		assertEquals(new BigDecimal("-0.300"), pnl.realizedPnl().setScale(3,  RoundingMode.HALF_UP));
		
		pnl.acceptFill(new BigDecimal("1.3"), new BigDecimal("-0.08"));
		assertEquals(new BigDecimal("1.3"), pnl.openPrice().setScale(1,  RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("-0.02"), pnl.openSize());
		assertEquals(new BigDecimal("0.10"), pnl.realizedSize());
		assertEquals(new BigDecimal("-0.060"), pnl.realizedPnl().setScale(3,  RoundingMode.HALF_UP));
	}
	
}
