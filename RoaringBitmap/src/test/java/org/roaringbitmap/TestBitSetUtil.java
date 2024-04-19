package org.roaringbitmap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class TestBitSetUtil {
  private static BitSet appendRandomBitset(final Random random, final int offset,
      final BitSet bitset, final int nbits) {
    for (int i = 0; i < nbits; i++) {
      final boolean b = random.nextBoolean();
      bitset.set(offset + i, b);
    }
    return bitset;
  }

  private static BitSet randomBitset(final Random random, final int offset, final int length) {
    final BitSet bitset = new BitSet();
    return appendRandomBitset(random, offset, bitset, length);
  }


  private void assertEqualBitsets(final BitSet bitset, final RoaringBitmap bitmap) {
    assertTrue(BitSetUtil.equals(bitset, bitmap), "bitset and bitmap do not match");
    assertEquals(bitset, BitSetUtil.bitsetOf(bitmap), "bitsetOf doesn't match");
    assertEquals(bitset, BitSetUtil.bitsetOfWithoutCopy(bitmap), "bitsetOfWithoutCopy doesn't match");
    assertEquals(bitset, BitSet.valueOf(BitSetUtil.toByteArray(bitmap)), "toByteArray doesn't match");

    RoaringBitmap runBitmap = bitmap.clone();
    runBitmap.runOptimize();
    assertEquals(bitset, BitSetUtil.bitsetOf(runBitmap), "bitsetOf doesn't match for run optimized bitmap");
  }

  @Test
  public void testEmptyBitSet() {
    final BitSet bitset = new BitSet();
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testFlipFlapBetweenRandomFullAndEmptyBitSet() {
    final Random random = new Random(1234);
    final int nbitsPerBlock = 1024 * Long.SIZE;
    final int blocks = 50;
    final BitSet bitset = new BitSet(nbitsPerBlock * blocks);

    // i want a mix of empty blocks, randomly filled blocks and full blocks
    for (int block = 0; block < blocks * nbitsPerBlock; block += nbitsPerBlock) {
      int type = random.nextInt(3);
      switch (type) {
        case 0:
          // a block with random set bits
          appendRandomBitset(random, block, bitset, nbitsPerBlock);
          break;
        case 1:
          // a full block
          bitset.set(block, block + nbitsPerBlock);
          break;
        default:
          // and an empty block;
          break;
      }
    }
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testFullBitSet() {
    final BitSet bitset = new BitSet();
    final int nbits = 1024 * Long.SIZE * 50;
    bitset.set(0, nbits);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testGapBitmap() {
    for (int gap = 1; gap <= 4096; gap *= 2) {
      for (int offset = 300; offset < 3000; offset += 10) {
        BitSet bitset = new BitSet();
        for (int k = 0; k < 100000; k += gap) {
          bitset.set(k + offset);
        }
        final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
        assertEqualBitsets(bitset, bitmap);
      }
    }
  }

  @Test
  public void testRandomBitmap() {
    final Random random = new Random(1235);
    final int runs = 50;
    final int maxNbits = 500000;
    for (int i = 0; i < runs; i++) {
      final BitSet bitset = randomBitset(random, 0, random.nextInt(maxNbits));
      final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
      assertEqualBitsets(bitset, bitmap);
    }
  }

  @Test
  public void testRandomBitmap_extended() {
    final Random random = new Random(1245);
    final int runs = 50;
    final int maxNbits = 500000;
    for (int i = 0; i < runs; i++) {
      final BitSet bitset = randomBitset(random, 100000, random.nextInt(maxNbits));
      final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
      assertEqualBitsets(bitset, bitmap);
    }
  }

  @Test
  public void testSmallBitSet1() {
    final BitSet bitset = new BitSet();
    bitset.set(1);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testSmallBitSet1_10000000() {
    final BitSet bitset = new BitSet();
    bitset.set(1);
    bitset.set(10000000);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testSmallBitSet10000000() {
    final BitSet bitset = new BitSet();
    bitset.set(10000000);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(bitset);
    assertEqualBitsets(bitset, bitmap);
  }

  /*
    The ByteBuffer->RoaringBitmap just replicate similar tests written for BitSet/long[]->RoaringBitmap
   */

  @Test
  public void testEmptyByteBuffer() {
    final BitSet bitset = new BitSet();
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testFlipFlapBetweenRandomFullAndEmptyByteBuffer() {
    final Random random = new Random(1234);
    final int nbitsPerBlock = 1024 * Long.SIZE;
    final int blocks = 50;
    final BitSet bitset = new BitSet(nbitsPerBlock * blocks);

    // i want a mix of empty blocks, randomly filled blocks and full blocks
    for (int block = 0; block < blocks * nbitsPerBlock; block += nbitsPerBlock) {
      int type = random.nextInt(3);
      switch (type) {
        case 0:
          // a block with random set bits
          appendRandomBitset(random, block, bitset, nbitsPerBlock);
          break;
        case 1:
          // a full block
          bitset.set(block, block + nbitsPerBlock);
          break;
        default:
          // and an empty block;
          break;
      }
    }
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testFullByteBuffer() {
    final BitSet bitset = new BitSet();
    final int nbits = 1024 * Long.SIZE * 50;
    bitset.set(0, nbits);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testGapByteBuffer() {
    for (int gap = 1; gap <= 4096; gap *= 2) {
      for (int offset = 300; offset < 3000; offset += 10) {
        BitSet bitset = new BitSet();
        for (int k = 0; k < 100000; k += gap) {
          bitset.set(k + offset);
        }
        final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
        assertEqualBitsets(bitset, bitmap);
      }
    }
  }

  @Test
  public void testRandomByteBuffer() {
    final Random random = new Random(8934);
    final int runs = 100;
    final int maxNbits = 500000;
    for (int i = 0;i < runs; ++i) {
      final int offset = random.nextInt(maxNbits) & Integer.MAX_VALUE;
      final BitSet bitset = randomBitset(random, offset, random.nextInt(maxNbits));
      final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
      assertEqualBitsets(bitset, bitmap);
    }
  }

  @Test
  public void testByteArrayWithOnly10000000thBitSet() {
    final BitSet bitset = new BitSet();
    bitset.set(10000000);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testByteArrayWithOnly1And10000000thBitSet() {
    final BitSet bitset = new BitSet();
    bitset.set(1);
    bitset.set(10000000);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), false);
    assertEqualBitsets(bitset, bitmap);
  }

  @Test
  public void testByteArrayWithFastRank() {
    final BitSet bitset = randomBitset(new Random(238), 0, 50);
    final RoaringBitmap bitmap = BitSetUtil.bitmapOf(toByteBuffer(bitset), true);
    Assertions.assertTrue(bitmap instanceof FastRankRoaringBitmap);
  }

  @Test
  public void testBitmapOfNegative() {
    final RoaringBitmap bitmap = new RoaringBitmap();
    bitmap.add(-1);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      BitSetUtil.bitsetOf(bitmap);
    });
    assertEquals("bitmap has negative bits set", exception.getMessage());
  }

  private static ByteBuffer toByteBuffer(BitSet bitset) {
    return ByteBuffer.wrap(bitset.toByteArray());
  }
}
