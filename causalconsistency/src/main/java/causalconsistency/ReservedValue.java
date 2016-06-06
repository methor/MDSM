package causalconsistency;

import java.io.Serializable;

public class ReservedValue implements Serializable {

	public static final ReservedValue RESERVED_VALUE = new ReservedValue();
	private static final long serialVersionUID = -5910996938335171091L;

	String s = "RESERVED_VALUE";

	private ReservedValue()
	{}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof ReservedValue);
	}
}
