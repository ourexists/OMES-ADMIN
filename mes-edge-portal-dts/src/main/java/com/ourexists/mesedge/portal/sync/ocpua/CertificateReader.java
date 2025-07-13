//package com.ourexists.mesedge.portal.sync.ocpua;
//
//import org.bouncycastle.cert.X509CertificateHolder;
//import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
//import org.bouncycastle.openssl.PEMKeyPair;
//import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
//
//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.security.KeyPair;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//
//public class CertificateReader {
//
//    public static X509Certificate loadCertificate(String filePath) throws Exception {
//        try (FileInputStream inStream = new FileInputStream(filePath)) {
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            return (X509Certificate) cf.generateCertificate(inStream);
//        }
//    }
//
//    public static KeyPair loadKeyPair(String privateKeyPemPath, String certPemPath) throws Exception {
//        // 1. 加载私钥
//        PrivateKey privateKey;
//        try (PEMParser pemParser = new PEMParser(new FileReader(privateKeyPemPath))) {
//            Object obj = pemParser.readObject();
//            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
//            if (obj instanceof PEMKeyPair) {
//                privateKey = converter.getKeyPair((PEMKeyPair) obj).getPrivate();
//            } else {
//                throw new IllegalArgumentException("Unsupported private key format in " + privateKeyPemPath);
//            }
//        }
//
//        // 2. 加载证书中的公钥
//        PublicKey publicKey;
//        try (PEMParser pemParser = new PEMParser(new FileReader(certPemPath))) {
//            Object obj = pemParser.readObject();
//            X509CertificateHolder certHolder = (X509CertificateHolder) obj;
//            X509Certificate certificate = new JcaX509CertificateConverter()
//                    .getCertificate(certHolder);
//            publicKey = certificate.getPublicKey();
//        }
//
//        return new KeyPair(publicKey, privateKey);
//    }
//}
