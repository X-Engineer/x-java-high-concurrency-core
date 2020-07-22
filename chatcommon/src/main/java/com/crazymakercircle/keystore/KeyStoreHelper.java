package com.crazymakercircle.keystore;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.crazymakercircle.util.IOUtil.closeQuietly;
import static com.crazymakercircle.util.IOUtil.readInputStream;
import static com.crazymakercircle.util.MathUtil.toHexString;

@Slf4j

@Data
public class KeyStoreHelper
{
    private static final byte[] CRLF = new byte[]{'\r', '\n'};

    /**
     * 密钥储存的文件
     */
    private String keyStoreFile;

    /**
     * 获取keystore的信息所需的密码
     */
    private String storePass;
    /**
     * 设置指定别名条目的密码，也就是私钥密码
     */
    private String keyPass;

    /**
     * 每个keystore都关联这一个独一无二的alias，这个alias通常不区分大小写
     */
    private String alias;

    /**
     * 指定证书拥有者信息。
     * 例如："CN=名字与姓氏,OU=组织单位名称,O=组织名称,L=城市或区域名称,ST=州或省份名称,C=单位的两字母国家代码"
     */
    private String dname = "C=CN,ST=Province,L=city,O=crazymaker,OU=crazymaker.com,CN=user";
    KeyStore keyStore;

    private static String keyType = "JKS";

    public KeyStoreHelper(String keyStoreFile, String storePass,
                          String keyPass, String alias, String dname)
    {
        this.keyStoreFile = keyStoreFile;
        this.storePass = storePass;
        this.keyPass = keyPass;
        this.alias = alias;
        this.dname = dname;
    }

    /**
     * 断言存在
     *
     * @return
     * @throws Throwable
     */
    public boolean isExist() throws Exception
    {
        assert (StringUtils.isNotEmpty(alias));
        assert (StringUtils.isNotEmpty(keyPass));
        KeyStore ks = loadStore();
        PasswordProtection protection = new PasswordProtection(keyPass.toCharArray());
        if (ks.isKeyEntry(alias))
        {
            PrivateKeyEntry userCert =
                    (PrivateKeyEntry) ks.getEntry(alias, protection);
            X509Certificate cert = (X509Certificate) userCert.getCertificate();
            if (new Date().after(cert.getNotAfter()))
            {
                return false;

            } else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 导出证书
     *
     * @param outDir 导出到目标目录
     */
    public boolean exportCert(String outDir) throws Exception
    {
        assert (StringUtils.isNotEmpty(alias));
        assert (StringUtils.isNotEmpty(keyPass));
        KeyStore ks = loadStore();
        PasswordProtection protection = new PasswordProtection(keyPass.toCharArray());
        if (ks.isKeyEntry(alias))
        {
            PrivateKeyEntry entry =
                    (PrivateKeyEntry) ks.getEntry(alias, protection);
            X509Certificate cert = (X509Certificate) entry.getCertificate();
            if (new Date().after(cert.getNotAfter()))
            {
                return false;

            } else
            {
                String certPath = outDir + "/" + alias + ".cer";
                FileWriter wr = new java.io.FileWriter(new File(certPath));
                String encode = new BASE64Encoder().encode(cert.getEncoded());
                String strCertificate = "-----BEGIN CERTIFICATE-----\r\n"
                        + encode + "\r\n-----END CERTIFICATE-----\r\n";
                // 写入证书的编码
                wr.write(strCertificate);
                wr.flush();
                closeQuietly(wr);

                return true;
            }
        }
        return false;

    }


    /**
     * 从文件加载KeyStore密钥仓库
     */
    public KeyStore loadStore() throws Exception
    {
        log.debug("keyStoreFile: {}", keyStoreFile);
        if (!new File(keyStoreFile).exists())
        {
            createEmptyStore();
        }
        KeyStore ks = KeyStore.getInstance(keyType);
        java.io.FileInputStream fis = null;
        try
        {
            fis = new java.io.FileInputStream(keyStoreFile);
            ks.load(fis, storePass.toCharArray());
        } finally
        {
            closeQuietly(fis);
        }
        return ks;
    }

    /**
     * 建立一个空的KeyStore仓库
     */
    private void createEmptyStore() throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance(keyType);
        File parentFile = new File(keyStoreFile).getParentFile();
        if (!parentFile.exists())
        {
            parentFile.mkdirs();
        }
        java.io.FileOutputStream fos = null;
        keyStore.load(null, storePass.toCharArray());
        try
        {
            fos = new java.io.FileOutputStream(keyStoreFile);
            keyStore.store(fos, storePass.toCharArray());
        } finally
        {
            closeQuietly(fos);
        }
    }

    /**
     * 创建密钥和证书并且保存到密钥仓库文件
     */
    public void createKeyEntry() throws Exception
    {
        KeyStore keyStore = loadStore();
        CertHelper certHelper = new CertHelper(dname);
        /**
         * 生成证书
         */
        Certificate cert = certHelper.genCert();
        cert.verify(certHelper.getKeyPair().getPublic());
        PrivateKey privateKey = certHelper.getKeyPair().getPrivate();
        char[] caPasswordArray = storePass.toCharArray();

        /**
         * 设置密钥和证书到密钥仓库
         */
        keyStore.setKeyEntry(alias, privateKey,
                caPasswordArray, new Certificate[]{cert});
        java.io.FileOutputStream fos = null;
        try
        {
            fos = new java.io.FileOutputStream(keyStoreFile);
            /**
             * 密钥仓库保存到文件
             */
            keyStore.store(fos, caPasswordArray);
        } finally
        {
            closeQuietly(fos);
        }
    }

    /**
     * 追加新的密钥到密钥仓库文件
     */
    public void appendKeyEntry() throws Exception
    {
        KeyStore keyStore = loadStore();
        char[] caPasswordArray = storePass.toCharArray();
        //获取CA
        PasswordProtection protection = new PasswordProtection(caPasswordArray);
        PrivateKeyEntry keyEntry =
                (PrivateKeyEntry) keyStore.getEntry(this.alias, protection);
        X509Certificate x509Cert = (X509Certificate) keyEntry.getCertificate();
        if (new Date().after(x509Cert.getNotAfter()))
        {
            throw new Exception("仓库已经过期");
        }
        CertHelper certHelper = new CertHelper(dname);
        /**
         * 创建证书
         */
        Certificate cert = certHelper.genCert();
        PrivateKey privateKey = certHelper.getKeyPair().getPrivate();
        Certificate[] certs = new Certificate[]{cert, keyEntry.getCertificate()};
        /**
         * 将私钥和证书设置到密钥仓库
         */
        keyStore.setKeyEntry(alias, privateKey, keyPass.toCharArray(), certs);
        java.io.FileOutputStream fos = null;
        try
        {
            fos = new java.io.FileOutputStream(keyStoreFile);
            /**
             * 保存密钥仓库到文件
             */
            keyStore.store(fos, caPasswordArray);
        } finally
        {
            closeQuietly(fos);
        }
    }


    /**
     * 导入数字证书到信任仓库
     *
     * @throws Exception
     */
    public void importCert(String importAlias, String certPath) throws Exception
    {

        if (null == keyStore)
        {
            keyStore = loadStore();
        }
        InputStream inStream = null;
        if (certPath != null)
        {
            inStream = new FileInputStream(certPath);
        }
        //将证书按照别名增加到仓库中
        boolean succeed = addTrustedCert(importAlias, inStream);
        if (succeed)
        {
            log.debug("导入成功");
        } else
        {
            log.error("导入失败");
        }
    }


    private Collection<? extends Certificate> generateCertificates(InputStream in)
            throws CertificateException, IOException
    {
        byte[] data = readInputStream(in);

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certs = factory.generateCertificates(new ByteArrayInputStream(data));
        return certs;

    }

    private Certificate generateCertificate(InputStream in)
            throws CertificateException, IOException
    {
        byte[] data = readInputStream(in);

        return CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(data));

    }

    /**
     * 将证书按照别名增加到仓库中
     */
    private boolean addTrustedCert(String alias, InputStream in)
            throws Exception
    {
        if (alias == null)
        {
            throw new Exception("Must.specify.alias");
        }
        //如果别名已经存在，则抛出异常
        if (keyStore.containsAlias(alias))
        {
            throw new Exception("别名已经存在");
        }

        // 从输入流中读取到证书
        X509Certificate cert = null;
        try
        {
            cert = (X509Certificate) generateCertificate(in);
        } catch (ClassCastException | CertificateException ce)
        {
            throw new Exception("证书读取失败");
        }
        //根据别名进行设置
        keyStore.setCertificateEntry(alias, cert);
        //保存到文件
        char[] caPasswordArray = storePass.toCharArray();
        java.io.FileOutputStream fos = null;
        try
        {
            fos = new java.io.FileOutputStream(keyStoreFile);
            keyStore.store(fos, caPasswordArray);
        } finally
        {
            closeQuietly(fos);
        }
        return true;
    }


    /**
     * Prints all keystore entries.
     */
    public void doPrintEntries() throws Exception
    {

        if (null == keyStore)
        {
            keyStore = loadStore();
        }
        List<String> aliases = Collections.list(keyStore.aliases());
        aliases.sort(String::compareTo);
        for (String alias : aliases)
        {
            doPrintEntry(alias);
        }
    }

    /**
     * Prints a single keystore entry.
     */
    private void doPrintEntry(String alias)
            throws Exception
    {
        log.info("{} 别名的证书信息如下：", alias);
        // Get the chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        if (null == chain)
        {
            if (keyStore.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class))
            {
                Certificate cert = keyStore.getCertificate(alias);
                printX509Cert((X509Certificate) cert);
            }
            return;
        }

        for (int i = 0; i < chain.length; i++)
        {
            if (chain[i] instanceof X509Certificate)
            {
                printX509Cert((X509Certificate) (chain[i]));
            } else
            {
                dumpCert(chain[i]);
            }
        }


    }

    /**
     * Prints a certificate in a human readable format.
     */
    private void printX509Cert(X509Certificate cert)
            throws Exception
    {
        String sigName = cert.getSigAlgName();
        log.info("Owner: {}", cert.getSubjectDN());
        log.info("Issuer: {}", cert.getIssuerDN());
        log.info("Serial number: {}", cert.getSerialNumber());
        log.info("Valid from: {}", cert.getNotBefore());
        log.info("Valid until: {}", cert.getNotAfter());
        log.info("Certificate fingerprints SHA1: ");
        log.info(getCertFingerPrint("SHA-1", cert));
        log.info("Certificate fingerprints SHA256: ");
        log.info(getCertFingerPrint("SHA-256", cert));
        log.info("Signature algorithm name: {}", sigName);
        log.info("Version: {}", cert.getVersion());
    }

    /**
     * Gets the requested finger print of the certificate.
     */
    private String getCertFingerPrint(String mdAlg, Certificate cert)
            throws Exception
    {
        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
    }

    /**
     * Writes an X.509 certificate in base64 or binary encoding to an output
     * stream.
     */
    private void dumpCert(Certificate cert)
            throws IOException, CertificateException
    {
        log.info(X509Factory.BEGIN_CERT);
        log.info(Base64.getMimeEncoder(64, CRLF)
                .encodeToString(cert.getEncoded()));
        log.info(X509Factory.END_CERT);

    }

}
