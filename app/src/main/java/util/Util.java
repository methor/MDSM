package util;

/**
 * Created by Mio on 2015/11/30.
 */
public class Util {

    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }


    public static int findCharPair(String s, int start)
    {
        char p;
        switch (s.charAt(start))
        {
            case '{':
                p = '}';
                break;
            case '[':
                p = ']';
                break;
            case '(':
                p = ')';
                break;
            default:
                return -1;
        }

        int count = 1;
        int index = start + 1;
        while (true)
        {
            if (index > s.length() - 1)
                return -1;
            if (s.charAt(index) == s.charAt(start))
                count++;
            else if (s.charAt(index) == p)
            {
                count--;
                if (count == 0)
                    return index;
            }
            else
                ;
            index++;
        }
    }
}
