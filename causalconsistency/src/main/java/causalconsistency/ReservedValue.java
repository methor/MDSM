package causalconsistency;

import java.io.Serializable;

public class ReservedValue implements Serializable {

	public static final ReservedValue RESERVED_VALUE = new ReservedValue();

	private static final long serialVersionUID = 4389614024986356038L;
	String s = "RESERVED_VALUE";

	private ReservedValue()
	{}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof ReservedValue);
	}
}
