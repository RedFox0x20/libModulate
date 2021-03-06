package libModulate;

// Assignment_ManyToOne
// Provide a method of storing conversions from an array of bytes (Key) to a byte (Value)
public class Assignment_ManyToOne {

	private byte[] Key;
	private byte Value;

	public Assignment_ManyToOne(byte[] key, byte value) {
		Key = key;
		Value = value;
	}

	public void setKey(byte[] key) {
		Key = key;
	}

	public byte[] getKey() {
		return Key;
	}

	public byte getValue() {
		return Value;
	}

	public void setValue(byte value) {
		Value = value;
	}
}
