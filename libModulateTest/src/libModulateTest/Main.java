package libModulateTest;

import libModulate.BitAssigner;
import libModulate.SymbolAssigner;

import java.util.Random;

import libModulate.Assignment_ManyToOne;
import libModulate.Assignment_OneToMany;
import libModulate.signals.FSKSignal;
import libModulate.signals.MFSKSignal;
import libModulate.utils.BitModifiers;
import libModulate.utils.DataModifiers;

// Suppress unused warnings as not all methods will be used when developing/testing
@SuppressWarnings("unused")

public class Main {

	public static void main(String[] args) {
//		Example_FSKSignal();
//		Example_MFSK_RepeatSymbol();
//		Example_MFSK16();
//		Example_MFSK32();
//		Test_Parity_First();
//		Test_Parity_Last();
		Example_NoisyFSK();

	}

	private static void Test_Parity_First() {
		System.out.println("RUNNING TEST: Test_Parity_First");
		byte[] Bits = new byte[] { 0, 1, 1, 0, 1, 1 };
		Boolean Result = libModulate.ErrorDetection.ParityCheckFirst(Bits, 0, 5);
		PrintTwoColumns("15", "\tExpected: false", "Result: " + Result.toString());
		System.out.println();
	}

	private static void Test_Parity_Last() {
		System.out.println("RUNNING TEST: Test_Parity_Last");
		byte[] Bits = new byte[] { 0, 1, 1, 1, 1, 1 };
		Boolean Result = libModulate.ErrorDetection.ParityCheckLast(Bits, 0, 5);
		PrintTwoColumns("15", "\tExpected: true", "Result: " + Result.toString());
		System.out.println();

	}

	private static void Example_FSKSignal() {
		System.out.println("RUNNING EXAMPLE: Example_FSKSignal");

		FSKSignal Sig;
		try {
			Sig = new FSKSignal(8000, 10, 100, 50, 0, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		SymbolAssigner FSKSymbolAssigner = new SymbolAssigner();
		FSKSymbolAssigner.setBitsPerSymbol(1);
		FSKSymbolAssigner.setAssignments(SymbolAssigner.DEFAULT_ASSIGNMENT_FSK_SYMBOLS);

		BitAssigner FSKBitAssigner = new BitAssigner();
		FSKBitAssigner.setAssignmentLength(1);
		FSKBitAssigner.setAssignments(BitAssigner.DEFAULT_ASSIGNMENT_FSK_BITS);

		byte[] SampleData = new byte[] { 'H', 'E', 'L', 'L', 'O', ' ', 'W', 'O', 'R', 'L', 'D', '!' };
		byte[] SampleDataBits = BitModifiers.UnpackByteArray(SampleData);
		byte[] SampleDataSymbols = FSKSymbolAssigner.ApplyAssignments(SampleDataBits);

		// XXX: TEST - Modulation of user-provided RAW BYTES
		Sig.setSymbols(SampleDataSymbols);
		Sig.Modulate();
		Sig.WriteSignalToPCM("RawData.8000.16b.pcm");
		Sig.Demodulate();

		byte[] DemodSymbols = Sig.getSymbols();
		byte[] DemodBits = FSKBitAssigner.ApplyAssignments(DemodSymbols);
		byte[] DemodBitsASCII = DataModifiers.ByteArrayToASCIISymbols(DemodBits);
		byte[] PackedDemodBits = BitModifiers.PackBits(DemodBits);
		System.out.println("Started with:");
		PrintTwoColumns("15", "\tRawBytes", new String(SampleData));
		PrintTwoColumns("15", "\tRawBits", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataBits)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataSymbols)));
		System.out.println("\nDemod to:");
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(DemodSymbols)));
		PrintTwoColumns("15", "\tRawBits", new String(DemodBitsASCII));
		PrintTwoColumns("15", "\tRawBytes", new String(PackedDemodBits));
		System.out.println();
	}

	private static void Example_NoisyFSK() {
		System.out.println("RUNNING EXAMPLE: Example_NoisyFSK");

		FSKSignal Sig;
		try {
			Sig = new FSKSignal(8000, 10, 100, 50, 0, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		SymbolAssigner FSKSymbolAssigner = new SymbolAssigner();
		FSKSymbolAssigner.setBitsPerSymbol(1);
		FSKSymbolAssigner.setAssignments(FSKSymbolAssigner.DEFAULT_ASSIGNMENT_FSK_SYMBOLS);

		BitAssigner FSKBitAssigner = new BitAssigner();
		FSKBitAssigner.setAssignmentLength(1);
		FSKBitAssigner.setAssignments(BitAssigner.DEFAULT_ASSIGNMENT_FSK_BITS);

		byte[] SampleData = new byte[] { 'H', 'E', 'L', 'L', 'O', ' ', 'W', 'O', 'R', 'L', 'D', '!' };
		byte[] SampleDataBits = BitModifiers.UnpackByteArray(SampleData);
		byte[] SampleDataSymbols = FSKSymbolAssigner.ApplyAssignments(SampleDataBits);

		// XXX: TEST - Modulation of user-provided RAW BYTES
		Sig.setSymbols(SampleDataSymbols);
		Sig.Modulate();
		double[] Samples = Sig.getSamples();

		// Artificially add some noise
		Random Rand = new Random();
		for (int i = 0; i < Samples.length; i++) {
			// Seems to be the limit of the demod before errors start to
			// overwhelm the data.
			Samples[i] += Rand.nextGaussian() * 0.5;
		}
		Sig.setSamples(Samples);

		// BUG: For some reason not reloading the file allows the demod process to occur
		// on the original sample set despite the samples being swapped for the noisy
		// version
		Sig.WriteSignalToPCM("Example_NoisyFSK.8000.16b.pcm");
		Sig.LoadSignalFromPCM16B("Example_NoisyFSK.8000.16b.pcm", 8000, 1);
		Sig.Demodulate();

		byte[] DemodSymbols = Sig.getSymbols();
		byte[] DemodBits = FSKBitAssigner.ApplyAssignments(DemodSymbols);
		byte[] DemodBitsASCII = DataModifiers.ByteArrayToASCIISymbols(DemodBits);
		byte[] PackedDemodBits = BitModifiers.PackBits(DemodBits);
		System.out.println("Started with:");
		PrintTwoColumns("15", "\tRawBytes", new String(SampleData));
		PrintTwoColumns("15", "\tRawBits", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataBits)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataSymbols)));
		System.out.println("\nDemod to:");
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(DemodSymbols)));
		PrintTwoColumns("15", "\tRawBits", new String(DemodBitsASCII));
		PrintTwoColumns("15", "\tRawBytes", new String(PackedDemodBits));
		System.out.println();
	}

	private static void Example_MFSK_RepeatSymbol() {
		System.out.println("RUNNING EXAMPLE: Example_MFSK_RepeatSymbol");

		MFSKSignal Sig;
		try {
			Sig = new MFSKSignal(8000, 10, 100, 50, 0, 1, 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// Shift the frequency up 1
		SymbolAssigner FSKSymbolAssigner = new SymbolAssigner();
		FSKSymbolAssigner.setBitsPerSymbol(1);
		FSKSymbolAssigner
				.setAssignments(new Assignment_ManyToOne[] { new Assignment_ManyToOne(new byte[] { 0 }, (byte) 1),
						new Assignment_ManyToOne(new byte[] { 1 }, (byte) 2) });

		// Shift the symbol down 1
		BitAssigner FSKBitAssigner = new BitAssigner();
		FSKBitAssigner.setAssignmentLength(1);
		FSKBitAssigner.setAssignments(new Assignment_OneToMany[] { new Assignment_OneToMany((byte) 1, new byte[] { 0 }),
				new Assignment_OneToMany((byte) 2, new byte[] { 1 }) });

		// Apply our shift to the sample bits -- This could be done within the following
		// loop however in more complex scenarios this method may be easier to maintain
		byte[] SampleData = new byte[] { 'H', 'E', 'L', 'L', 'O', ' ', 'W', 'O', 'R', 'L', 'D', '!' };
		byte[] SampleDataBits = BitModifiers.UnpackByteArray(SampleData);
		byte[] SampleDataSymbols = FSKSymbolAssigner.ApplyAssignments(SampleDataBits);
		byte[] SymbolsWithoutRepeats = FSKSymbolAssigner.ApplyAssignments(SampleDataBits.clone());

		// Look for duplicate symbols, replace our duplicate with our repeat symbol (0)
		for (int i = 1; i < SymbolsWithoutRepeats.length; i++) {
			if (SymbolsWithoutRepeats[i] == SymbolsWithoutRepeats[i - 1]) {
				SymbolsWithoutRepeats[i] = 0;
			}
		}

		Sig.setSymbols(SymbolsWithoutRepeats);
		Sig.Modulate();
		Sig.WriteSignalToPCM("RawData_RepeatOnZero.8000.16b.pcm");
		Sig.Demodulate();
		byte[] DemodSymbols = Sig.getSymbols();

		// Detect the repeat symbol (0) and swap it for the previous symbol
		for (int i = 1; i < DemodSymbols.length; i++) {
			if (DemodSymbols[i] == 0) {
				DemodSymbols[i] = DemodSymbols[i - 1];
			}
		}

		byte[] DemodBits = FSKBitAssigner.ApplyAssignments(DemodSymbols);
		byte[] DemodBitsASCII = DataModifiers.ByteArrayToASCIISymbols(DemodBits);
		byte[] PackedDemodBits = BitModifiers.PackBits(DemodBits);
		System.out.println("Started with:");
		PrintTwoColumns("15", "\tRawBytes", new String(SampleData));
		PrintTwoColumns("15", "\tRawBits", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataBits)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataSymbols)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SymbolsWithoutRepeats)));
		System.out.println("\nDemod to:");
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(Sig.getSymbols())));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(DemodSymbols)));
		PrintTwoColumns("15", "\tRawBits", new String(DemodBitsASCII));
		PrintTwoColumns("15", "\tRawBytes", new String(PackedDemodBits));
		System.out.println();
	}

	private static void Example_MFSK16() {
		System.out.println("RUNNING EXAMPLE: Example_MFSK16");

		MFSKSignal Sig;
		try {
			Sig = new MFSKSignal(8000, 10, 100, 50, 0, 1, 16);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// Shift the frequency up 1
		SymbolAssigner FSKSymbolAssigner = new SymbolAssigner();
		FSKSymbolAssigner.setBitsPerSymbol(4);
		FSKSymbolAssigner.setAssignments(SymbolAssigner.DEFAULT_ASSIGNMENT_MFSK16_SYMBOLS);

		// Shift the symbol down 1
		BitAssigner FSKBitAssigner = new BitAssigner();
		FSKBitAssigner.setAssignmentLength(4);
		FSKBitAssigner.setAssignments(BitAssigner.DEFAULT_ASSIGNMENT_MFSK16_BITS);

		// Apply our shift to the sample bits -- This could be done within the following
		// loop however in more complex scenarios this method may be easier to maintain
		byte[] SampleData = new byte[] { 'H', 'E', 'L', 'L', 'O', ' ', 'W', 'O', 'R', 'L', 'D', '!' };
		byte[] SampleDataBits = BitModifiers.UnpackByteArray(SampleData);
		byte[] SampleDataSymbols = FSKSymbolAssigner.ApplyAssignments(SampleDataBits);

		Sig.setSymbols(SampleDataSymbols);
		Sig.Modulate();
		Sig.WriteSignalToPCM("Example_MFSK16.8000.16b.pcm");
		Sig.Demodulate();

		byte[] DemodSymbols = Sig.getSymbols();
		byte[] DemodBits = FSKBitAssigner.ApplyAssignments(DemodSymbols);
		byte[] DemodBitsASCII = DataModifiers.ByteArrayToASCIISymbols(DemodBits);
		byte[] PackedDemodBits = BitModifiers.PackBits(DemodBits);

		System.out.println("Started with:");
		PrintTwoColumns("15", "\tRawBytes", new String(SampleData));
		PrintTwoColumns("15", "\tRawBits", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataBits)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataSymbols)));
		System.out.println("\nDemod to:");
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(DemodSymbols)));
		PrintTwoColumns("15", "\tRawBits", new String(DemodBitsASCII));
		PrintTwoColumns("15", "\tRawBytes", new String(PackedDemodBits));
		System.out.println();
	}

	private static void Example_MFSK32() {
		System.out.println("RUNNING EXAMPLE: Example_MFSK32");

		MFSKSignal Sig;
		try {
			Sig = new MFSKSignal(8000, 100, 100, 50, 0, 1, 32);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// Shift the frequency up 1
		SymbolAssigner FSKSymbolAssigner = new SymbolAssigner();
		FSKSymbolAssigner.setBitsPerSymbol(5);
		FSKSymbolAssigner.setAssignments(SymbolAssigner.DEFAULT_ASSIGNMENT_MFSK32_SYMBOLS);

		// Shift the symbol down 1
		BitAssigner FSKBitAssigner = new BitAssigner();
		FSKBitAssigner.setAssignmentLength(5);
		FSKBitAssigner.setAssignments(BitAssigner.DEFAULT_ASSIGNMENT_MFSK32_BITS);

		// Apply our shift to the sample bits -- This could be done within the following
		// loop however in more complex scenarios this method may be easier to maintain
		byte[] SampleData = new byte[] { 'H', 'E', 'L', 'L', 'O', ' ', 'W', 'O', 'R', 'L', 'D', '!' };
		byte[] SampleDataBits = BitModifiers.UnpackByteArray(SampleData);
		byte[] SampleDataSymbols = FSKSymbolAssigner.ApplyAssignments(SampleDataBits);

		Sig.setSymbols(SampleDataSymbols);
		Sig.Modulate();
		Sig.WriteSignalToPCM("Example_MFSK32.8000.16b.pcm");
		Sig.Demodulate();

		byte[] DemodSymbols = Sig.getSymbols();
		byte[] DemodBits = FSKBitAssigner.ApplyAssignments(DemodSymbols);
		byte[] DemodBitsASCII = DataModifiers.ByteArrayToASCIISymbols(DemodBits);
		byte[] PackedDemodBits = BitModifiers.PackBits(DemodBits);

		System.out.println("Started with:");
		PrintTwoColumns("15", "\tRawBytes", new String(SampleData));
		PrintTwoColumns("15", "\tRawBits", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataBits)));
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(SampleDataSymbols)));
		System.out.println("\nDemod to:");
		PrintTwoColumns("15", "\tSYMBOLS", new String(DataModifiers.ByteArrayToASCIISymbols(DemodSymbols)));
		PrintTwoColumns("15", "\tRawBits", new String(DemodBitsASCII));
		PrintTwoColumns("15", "\tRawBytes", new String(PackedDemodBits));
		System.out.println();
	}

	private static void PrintTwoColumns(String length, String left, String right) {
		System.out.printf("%-" + length + "s %s\n", left, right);
	}
}
