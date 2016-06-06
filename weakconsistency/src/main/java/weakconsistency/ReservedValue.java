package weakconsistency;

import java.io.Serializable;

public class ReservedValue implements Serializable {

	public static final ReservedValue RESERVED_VALUE = new ReservedValue();
	private static final long serialVersionUID = -4766803960286340279L;

	String s = "RESERVED_VALUE";

	private ReservedValue()
	{}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof ReservedValue);
	}
}
