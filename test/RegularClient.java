import java.io.IOException;
import java.net.InetSocketAddress;

public class RegularClient extends SampleClient {
    public RegularClient(InetSocketAddress address) throws IOException {
        super(address, null);
    }

}