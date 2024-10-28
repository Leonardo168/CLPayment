package webClient.mercadoPago.teste.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RequestSignatureService {

    @Value("${MP.Signature}")
    private String mpSignature;
    
    public boolean validateRequest(String xSignature, String xRequestId, String dataId) {
	if (xSignature == null || xRequestId == null || dataId == null) {
	    return false;
	}
	
	String[] parts = xSignature.split(",");
	String ts = null;
	String receivedHash = null;
	
	for (String part : parts) {
	    String[] keyValue = part.split("=", 2);
	    if ("ts".equals(keyValue[0].trim())) {
		ts = keyValue[1].trim();
	    } else if ("v1".equals(keyValue[0].trim())) {
		receivedHash = keyValue[1];
	    }
	}
	
	String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);
	
	try {
	    Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
	    SecretKeySpec secretKeySpec = new SecretKeySpec(mpSignature.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	    hmacSHA256.init(secretKeySpec);
	    byte[] hashBytes = hmacSHA256.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
	    StringBuilder hashBuilder = new StringBuilder();
	    for (byte b : hashBytes) {
		hashBuilder.append(String.format("%02x", b));
	    }
	    String calculatedHash = hashBuilder.toString();
	    
	    return calculatedHash.equals(receivedHash);
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }
}
