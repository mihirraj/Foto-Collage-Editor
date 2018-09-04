// Copyright 2010 Google Inc. All Rights Reserved.

package com.wisesharksoftware.billing;

import com.wisesharksoftware.billing.Consts.PurchaseState;
import com.wisesharksoftware.billing.util.Base64;
import com.wisesharksoftware.billing.util.Base64DecoderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {
    private static final String TAG = "Security";

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * This keeps track of the nonces that we generated and sent to the
     * server.  We need to keep track of these until we get back the purchase
     * state and send a confirmation message back to Android Market. If we are
     * killed and lose this list of nonces, it is not fatal. Android Market will
     * send us a new "notify" message and we will re-generate a new nonce.
     * This has to be "static" so that the {@link BillingReceiver} can
     * check if a nonce exists.
     */
    private static HashSet<Long> sKnownNonces = new HashSet<Long>();

    /**
     * A class to hold the verified purchase information.
     */
    public static class VerifiedPurchase {
        public PurchaseState purchaseState;
        public String notificationId;
        public String productId;
        public String orderId;
        public long purchaseTime;
        public String developerPayload;

        public VerifiedPurchase(PurchaseState purchaseState, String notificationId,
                String productId, String orderId, long purchaseTime, String developerPayload) {
            this.purchaseState = purchaseState;
            this.notificationId = notificationId;
            this.productId = productId;
            this.orderId = orderId;
            this.purchaseTime = purchaseTime;
            this.developerPayload = developerPayload;
        }
    }

    /** Generates a nonce (a random number used once). */
    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }

//    public static ArrayList<VerifiedPurchase> verifyPurchase(String signedData, String signature) {
//    	return verifyPurchase(signedData, signature, null);
//    }
    	
    
    /**
     * Verifies that the data was signed with the given signature, and returns
     * the list of verified purchases. The data is in JSON format and contains
     * a nonce (number used once) that we generated and that was signed
     * (as part of the whole data string) with a private key. The data also
     * contains the {@link PurchaseState} and product ID of the purchase.
     * In the general case, there can be an array of purchase transactions
     * because there may be delays in processing the purchase on the backend
     * and then several purchases can be batched together.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static ArrayList<VerifiedPurchase> verifyPurchase(Context context, String signedData, String signature, String string) {
        if (signedData == null) {
            Log.e(TAG, "data is null");
            return null;
        }
        
        if (Consts.DEBUG) {
            Log.i(TAG, "signedData: " + signedData);
        }
        boolean verified = false;
        if (!TextUtils.isEmpty(signature)) {
            /**
             * Compute your public key (that you got from the Android Market publisher site).
             *
             * Instead of just storing the entire literal string here embedded in the
             * program,  construct the key at runtime from pieces or
             * use bit manipulation (for example, XOR with some other string) to hide
             * the actual key.  The key itself is not secret information, but we don't
             * want to make it easy for an adversary to replace the public key with one
             * of their own and then fake messages from the server.
             *
             * Generally, encryption keys / passwords should only be kept in memory
             * long enough to perform the operation they need to perform.
             */
        
        	int id = context.getResources().getIdentifier("security_key", "string", context.getPackageName());
            String base64EncodedPublicKey = context.getResources().getString(id);
            
        	//InstaLomo public key
            //String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAquWCvyF8MZdJJkE+hrsyHmVAG7+tuizj7WUG5RKO0AU6tGJBJEyC1nq4hc/e8wo1h7gpLD6waiSGIMP4TxaPT2xkisxlQWUMyrQLJGXcUHKNBD3s+YTmu7XF+K3hBYgjsbxS9iHsdcRRKhHmEIJs6jXKdjD/2SNGfaWYJU9TuXl6xsf2GjVrbVPzsDs8DfUOQCS2z0gmtw4MGCcxs4BKKQZA+kKAJkthNtiMNU7eFps1qJG6K1X3bJUbiRgqE7FRBkDLykvlvVFJH6mZLve4mTVWnfyvKdklnAAQz8GO7xILOagg9E9rRVoUrbKxFi/PBtAE4rz/oDdJ2oy31m1KUQIDAQAB";
            
        	//LomoCamera
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgUoIMQd7TdLhmmt1s+vitQKwjpsLBgmgF513PMcx0PIY4O/wHS1ubSYPKzLTZ649ERO39HGpr247iBE8wlijA7xFWX760SQ5VlLGVVgh92YpDWv+J1KsKULOhKg8FD3KedBuREGCIF38WpmfyXxjnOoKCrx1ozK5MOJZaY1HN0EBDxdWv6w7akS6rFJMSw7UvVNTV+H64EwqdriH6hMJ6YDbjdYTRtkXGY9AVsVz5zcbi8Pm/1RVzSiGEirNWajzla8nSgrzlohO0Qj3ovaPPBqrZtxOE4ArdFaTSK7XR5jIP3/zN4+Jxdiw4EiqNHjreJuXv1jFU2Fnl7wu0HhwywIDAQAB";
            
        	//StereoCamera
        	//String base64EncodedPublicKey ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh+MHABfV1+0XNUopSBAsfRMEcXcCu+JIiCgDObj2IrtiuWVyAqyM2QNiDzZm3cCvMi8Io+WVVc4bzdXH6QvF/zXgOtig/MPnvQ9djGl3FLXjbZ8zj/PHq5dCvHzF0y+zO8d09NHgC46XaRIG70/CwXFq17s4HbCgs67f6HPMrBZFiFd1Zk2VBXBn5kDLy2NtCuiQRSOuSsmlw9KBBPcnfp29wkIHhAy6CtLpg8cYhNAsWyWwOGGGtxcjWY7EcbsnBrUDPZVn0H7Ok/D+LKJV0BMcxaiV3dgQ/goMw9jt++T2XL5oTioOh3erwPvjWjPlXxwPDCyhfuz/7HMmjEiCUQIDAQAB";
        	
        	//InstaRetro
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlBfNgZ31wujhU4d5AiONarBnKfeZtoUMOyxG1A8fVmAQ1tFa7+zb+VeD2gLkaaRwgTsXIqg14LhChtSxThAndkNng3zANiJ8xGwdk/i36yrLvseTri1Zj/m+DztSKoM1r98T7F65KmXmm7k4XM/h7aYAsBbWTp0y/Wv4YT6XWQCgzrc1A9Aiixq63T4rwScIVqTejLnwr+DwLnU4u/lRF0M+H82qNfCnOEXvOoviRKhGl4yqmFOWiCm8qxeT5QdtB9fKKHm51lzaNy2d9MqqmKOIaD+ihuYZO0SxxooULXKrqR7GDAU25zSzeHiUbrZGU8kh8o5zyUY3V7WcxOhvkwIDAQAB";

        	//InstaPicasso
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAudD5lhegvLkErEGmVrz848x4lRuls5owM9dom464UHmo7mKBelSuIVAdTnvt7G/qvvM2o/3qxJh4QNDpeiUlC1lCzBIpF4J8xN2I81059dmo8LLsd1tNT/k6qguxHOWN3NS5yoaPlkKnPoy9uqkXYmDtJyjYg3CabBqufJHhtJnqhk5nrpWqUnSJI96mSBD+nZcDX/pMH96+aXbijNTsSrkpwJNTVViyhe3UcPiuVtkMXqXMB9vHCEtJSy12cEZneBn4bTqHxk68ccZItYxFsxsfYo/6NlTLfaRNeAt4fDPIKAJUmhgZSb2bkTf+rMy3EQHaVYai3k6qmjXYNaWOGwIDAQAB";

        	//InstaFish
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm7EW522siDDJHgCk4NXsheJRGlwBByaz8naOajvvisCtXmCysTnXDO1/i25ktoysFufRXAzOyvDM8zMddWdSFXglfWJxQcw1HXVHr14RPL+r7M0wj6P1Xk/+syAI/Xp+wFHK89gmShzzm5KFCKqNnQfyyFCaSp0Lj7FUaAQJGX/BzqjjbGC0GwP/Wr8/AbL3LwOo8uFNN4zF27XjIVNsBDDLoHqFTylTOo7EYXYPzFn3XRlIV8MN7/vfP/PNeEZmRsyVQWWtm48vfOy7sioYw+ir1I1SD84sz7CnOEp5rTjck54Bsyc8yKrFbvsqCsAIrfiqrLXechtrrd1jgvz0mwIDAQAB";

        	//InstaCartoon
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAicG2Z2V9Tg8ElhPHQWVSo9Zzl+AsY9mZbPRBzmKbeM04/GooBTJWXUP44//JEFd66nHaJore1K71AwyMErI5gS+6U1a7PlzaPuhioVY5Nvm2hdp+WJfk7i+R6YLLPFIY0a/ERMCgNsDdu/jGfBL+Cl4fV0T8DM2d6FtyUXKe2kNWody/5g/XxG1dHLTUYyv2xucrCvrjUGbU84qBNQcPViCYvl+wIvaa89Svo6OCi/gfZ8kFbnnc54Hu4JSIlpoFTJHpZboknPuVhdRTJPM3sH/A+cqGsFgwxjqk+SlG3q29v7ZBt6X8vdZduXFrU3NnCVwPpuXCJYOMTDR3dXYFQwIDAQAB";

        	//InstaSketch
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjpQKE5eFU+mLEYuFaX4ITn+tGAl/ot6Ey+DrhWNxT8zHTHmLol5biww4Fwx8cvjdrNQCPtWmGFVFXsNigZfkhP/YwQBuPy+3YmZNFdU1emQUn9/6wKBC7MNKVp9cbUsIOBLeAmtfhbsNhBmwv8S9sc1aLEKraS0c6V45DvN95bKVbO9GWq4DwBbLPoGH79uBStQPnKzMaHcZiCaOVe+BEqO0zY3w47iUTvsxGZr7SufDFJ10L0c3jRE1xZ52EQ1kTKiQQxa5NPfFwEPPE9UC/gY+BKljl8DXt0axnd6HdGUYG6+hPjsa3wbo6kN71Uf5QCW3aJ2Eig682+4Rnv9LCQIDAQAB";

        	//InstaLomo HD
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmXBLuIyWyb6qdvi5CKAt1FgsOlIjlOsehzjVhm876VFOy8zMGn2Jd1L8Hk+d0NtJArz7zsnpCzo1E1fYDcBKi6IEjjxofSASm3BmUuWdWVl0tpbPI+iKf/jWVi802ytLnduKODpMlCEmhumlqnE2cwhzWlrcxMtDE3GSpsIr1j8aILKNfkVcV6t8DBkF38OSzn/mo4UvYcGSotRuc83nHoJSi0nqk4OoJpfBrgzut3Lv5D5DMuyiMZdbD6SaXyullKF/Y08s6c6MCZ3ajauJ8t8dtaKgXpYaFTU2q/U9dX3Jzba2Udrq+QkcrA4fZ0B34UBrm5MVPdiCIcrFk5u/rQIDAQAB";

        	//Cartoon Camera
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmy3WvZBlnl0mZPvsZDkzs9vcrn8dn5J9ixILPPLetiW4I3UW4tKEj6r5BFd5B7/pHlqFvtKL5Ej+xnMaWes4dxPLu5XW//isNpQdHRRiXckVJ1UwONXnmrWEv5ol9MSmlRy0fbMM1DtxqeiGouyoBQfYV7QPf8zpO9jjNE0H9aFqbF7q8UltknwOXhweDoGIHRKmrdrUkjxCJtrCsJR4YC7aJrCYSCMHwG+25rM5D3QoH4O02oDxfQ4Zz2nDWRTlO30jGCakDrRVhEiggiN7tn2ccPlhIohfSNFd1Wg0isafC1dJk5wJ+uf5gePqye/B1NtkoGO0c/ndkSI2yu7EdQIDAQAB";

        	//Retro Camera
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsc3ECp9LrgvRm/hdtzaeoAl1TSvye2rQbw2VOKSasqxdAEmspDVKVJo+QBsXFTgdMD4GPaAE2eDwOrUVrtlFrxcNYoGvD14VeIH7J/hmrA/CZ7xWa5ZK79tToPlxcFNGt/GGxbcU8qk09SgYBtVE1tOivXeuILFcppWBaK2kYHHMPjrmQi8r1N+W2LXHSu+0mrvBDNcDnRfKkSzhzSE1mq11OE9qOQjSyXxHhKZb4iL4xgr3C4Nooya8FEkqrozFrbNVXqIhkbA8OzigPL4mkl6jVROKkGA3zZSY3aKcprPij5ECyixNVsfuEELw+nQb64Bf/ZdoesF7x5eAF/BLBwIDAQAB";

        	//InstaEffects
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoJMAvjiQs2F3ViwikYs0FFERld2PSZ66KCdfpwAI113aaI+EUapPEIfS3r315zY9JWupThPBnQAzwRJ8WeCAUNpXDbTCPGEQW6/CpsjpuCpW4M61FssnPVV/GR+d40J7DQl2lY9ww0ok2wLsC9Iggx+tZZK/eUVtsUdkjzlnkQ7QbhcJWMVOT2HeYwovVHPykLRWYWMRJqVcLDoRs493w0Hzsye/ikmIQAXnsAipK4QI1GBVlBsJVufXahYW34UHT+RRryM+4dvIK+QEprC28JW2PeIZQOgvQZDmua5fhgsLU/zxJ/4gFqphH/ba2KxaP/BpayXNCUbMLMGax8qfCQIDAQAB";

        	//3D Camera
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh+MHABfV1+0XNUopSBAsfRMEcXcCu+JIiCgDObj2IrtiuWVyAqyM2QNiDzZm3cCvMi8Io+WVVc4bzdXH6QvF/zXgOtig/MPnvQ9djGl3FLXjbZ8zj/PHq5dCvHzF0y+zO8d09NHgC46XaRIG70/CwXFq17s4HbCgs67f6HPMrBZFiFd1Zk2VBXBn5kDLy2NtCuiQRSOuSsmlw9KBBPcnfp29wkIHhAy6CtLpg8cYhNAsWyWwOGGGtxcjWY7EcbsnBrUDPZVn0H7Ok/D+LKJV0BMcxaiV3dgQ/goMw9jt++T2XL5oTioOh3erwPvjWjPlXxwPDCyhfuz/7HMmjEiCUQIDAQAB";

        	//Fisheye
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0MXO5szijswNrE6ExwghvqzUxkSeMH4IRp059Ga2Y7oAvMlEVjTmUeUnjS7WfQq4jK8GAM0y/R34WMvVMFNtOWcINwfZbgUK2rnj46V1bOyzIjKPwgy9y1pNJ/lOQP4y/u0IzbAnX1fr/xQNRPuIvYQre9cdjQY95Cezbh/CAGHVZnC0Sw3DcFy7rXcLNVSt2gSUE2WCBXoBDq3nKGFzP7GSyhUs0R4M3rAYO+c1+hKDBavzyyjXAgQa82Hz/G3R1rpcfUTWWzkkhVVAi7VJ0rlqwUQ7NdPTrW5seD9+OKGlui008JftwOmMXAePMzyaO/LGX9Y70aMwCIDKttuxlwIDAQAB";

        	//Animal Faces
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgmAIvAWaBHNWnQP4q3Rs9q8ySAEoifJtVqJD6mQbSR54ctFlkPoZ7YoLMmruwctwd3YGYAh0XJZPmVaTurKHkgslPYhS1/x81y98N43cUaRidbjo2W7Xr1G+uJyvpc7PZF/R5YZrRfXditCMG8EJt/cLGCfBjVvOfUFLxwFmDRL+uqNkUNaAfutFBjHExeodZdbmxYzk124b1udqGOkfH5LM6eIOrhfp88E+gNW77llpyqKiHa9J7rjeMLU7x8rb2FHY5qAFCItPa2hfAQVPe4XpcJ2m9ifYp53phJz6zClBGKFeLvCmtOr24OjAExPaBpbh1KZ7eHHL+Cmz/SrrvwIDAQAB";

        	//HDR
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsb/70yJvtTCjBo4PTKdpJ1+Nj2ItAoqCwa8+k6bKsVUmz7kfspFblyuzXgY5EuLl7+Od8k0UfipRKlSJjIuu45uvRZkV6gNFH6EWF+QO9fPMuVhy2tT29FmwBX/ChPg8+af/xx4oIMDANyJjxxABsUVfwcBZ4bzPoiVFHEa2216vLAYuOHGd8ljouU8OhJ3lD9ZbRpPh5NjNQjgzdE+OngdNGZbuU+nzSCETFxTwcYM7NHup/QjlvZGC6f8wudpBIZVqMZlnLM4pk7sGfqUDvJjxALsRPx73cz6ltwuzVizyk1oUULwxV6fBqgQbp3Le53NrB/x+NkbXI82k3VhLuQIDAQAB";

        	//PicBeauty
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn8Uj1Y1FyvTQEQtoptck2FBa70XFmZLBxrso6uXXdMomgPkb4nI2jUG48AwlTUSGTPLMngOExn++k/WwLiFhAEL4YzktPo+V0WOfX1WyD/0k+Cu+e5bwgQ8+z0ZxKfCeVOO6wmK2yBvrb8J5JM16LPMbtgWPZUhlLppUNo6U+7E1tJs3Gjh7PSlY6tNIsArMH46/wXGkc9oaBe8XO2/wK7r7eNva7kgcNbAfUX2Cg6Ktoz/LEZoTNNCEKU0UBiqQpnwEhFQM/gvxMon7+dGYrxgjJrT3Rt8hRoNBg+iY/4vXvvK8vQH8y6LCTeSSIXfN62Gu3FnASX39WpsnNM3ZpwIDAQAB";

        	//FaceTune
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAka7dUbE82OwWFOg5AYbYdl/MREt81O46cTGu0HtnhigZR9+1/pf4gaJArdVJCFiBCvs0FUQQ/I5l6zjchYV0RfRGoA1VeGHGn1AXXxUIoUxTTrIz4y0CnnOqJCq4llfPNTCeG7wgIDhs60//nRm7d5AIJFzhqvDwYO7+OQ+NU8QXP+pPde6+rxjQsLsIfNMZ24r+mlyPM6fEgzQ6O8cwoBibxltCTM8vRAAntxWZ15wreQze4XbH9pEZE8mtfOjdVCiUryaZ6WgJ9OrGcdbq5VubK6gcjmge/0MKOHIELBrnq3Vc/5VMmMAAjl+rcb1jERZNs4k4yq1uMyDT34UECQIDAQAB";
        	
        	//AfterLight
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiONlcAwXUPiNVEVhYXK2h7+MPeCB7s0q2JJg6FJR2B7o0NM2FVy4jwkAOmww/vImIXGWlk9Wf/4kVB/p1WRs5rQqTsBM+dEHIzfAsQBIMa5bwlfmC1hOHJ0IQ/ruMYMP/2i227CRyKEaPdaQ6Q9kOvmez9fdRTbNkyPYktSmV+7KjZgpbmzviLRBrAhAjl3lGW7dJ1O+LS1Stqd12smOnV+YoN2rxaBk19P2yPVhSajh/hk8KI5Wr/K6++aGUJLeikeShTDnqUTDuxUcjyDPpdvDCwfoiTi9YKTn8se4hvtrs/1aaAA+kTfyviMK3WqE6uouCJ80b+LhGdxp0IOK4wIDAQAB";
        	
        	//Filtergram - SharkyFilters
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoAcVtKYBdX3yNUW+ml49JOF5Xv10ertziBFlnZuCKDgkr4GFXWjmp+dUeZT3DAGGuABJxlU4ZZBpdr+ZEuyXDNUr2ie5RW8EP8TBTaLHg4AOKE8jEuM4Pes3R+PUXPc6JSV2tK7OPqMdZsnFzLkLvFpt7nJVeo75etStef6CaaJzjvhBTd8JAVuo2TDYiSVI7Ti1yLuln+5tW+aQpZ5htgd7AHNtc0/Q+0mUzfNUoOcbegONw/r6rE2Sgd4x1/fhHkD1gmjtLjWZXJV4apQ45aypH+Y9tW25rKBspg8hdntvRbkxsIFttZsvthmUnrjmo/sG6AXnANGDz1a6YAk/UwIDAQAB";
        	
        	//Filtergram - badbigsoftware
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk62yg2D/4TIRMugWWTB8bTpPlF1EuewTxrAH0C/m6sVzLMYLNtA644Buqm2gbM9GkGJYwW1/phNl7jdSY3vyb+dvPi4zKK4+zgkdS2s5nsP3gMKnP8bM9d4ToYIida2xS1ZvpDPGVkHJsVdNwp1weQoYetu/0UVhtC8UAQMEJQ+AEzyC6h6kpJudHvEZ/xfMpBgDIszuymeIqWPUAESEpyhg8ZTcM3XI2hY6R3vxb1JOpJ2q7U3quWTdfjWKfWJE6K0jozqE4xBanUR5e1o2omgZHlMXKGwTBk+MYWDUIoe3goWmHiTQMHyupA/a3VitSbvaQNssMcA442lxG1hv3wIDAQAB";

        	//Fotor
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApzgkKcfFIIat9ZhZNzlEvZcArWzv9zLvvTWTe8F7yGCzp+ooFdaH50xo2KXsbNBgEOdKnr4SSXONJuWTEohA/ktffsv2QrfosFsqnZ7jN8nQvR/ayfJXgJLrUgOI2S/ooNH4w8QsFDaSDipQ/Mf+SoKn5YRikbcdZoFgiV64nznmCajIJYW9AKBlRS+9wuIBZ1GUEY2i2Uo7XqOd9TayI5KBLpSzWzB+ow/X/wM5q/wh8vR2Ephe9SGHtrptvDU/k4MTegGK3RnC08xAlGGjfMbpTzH37ltK1dJiKBoq0Dgm3y0uyx4kH4t+qbCbsw+CSAMEhNQAIdb2/BZBVc6DAwIDAQAB";
        	
        	//Rookie
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgka9M8JNTi5gEcCW5sNycd6hwzMHm7/gxwrrQjpoBWME+8RfSr83KQPjWOoj7NUyPMkTmlUOc9TambZwUHQmjUwzYQAEh5JQVBQKj+o3qV6SWGP1VHoEk7SmlWKw6CPit8gLuRKrRiGM2A2tnCdSYWrMpO1fubmyyylqdk7iNYCdejsDL1Z0UeAWgFuMyKMsj/EFX9AmeHfgxVx2/crPLq1rgL0irvynatJkMCOzRZ1JUpb5T/4KaH0dPQM6S9KAYVJuD8ENRq3pFS6durtpY7qGUzAsGdvgfOJ2bg/DScDi/zX5MHdV3J2dUw5rt9djZiG+CBfgv6S/L8rrO78XQIDAQAB";
        	
        	//TurboScan
        	//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtth5dTaQ8xxNkUS9mXiXDHRqhWXkg5OqT/3UbHeT+Xu35Y8Way8TzaCoWzJI72cGWVuE+KdJvC+zTG+JBy1M2T5aTMUmiKDx3H51PD08g3sWmeqeG7TwF6gBfV6E1g5AyKm0RWStPbl84lUZug6jdlq1ygqRlyfq9E6TXCMmt5zSu5+qrTcIGN6m9EOqtv9SMsatjcvlUV+nVtbUzCYwlHb2wYSPbN1M2LtT0cOdyRDjTWOAXk82ZMvSRwnkD6j83/bos15ebmf058HrZq41F+Up3FfkWIDiSbrc6JYUiMr+j+pw1A8DTEHusLZ56fjd0AAGF+Sw7ygQW/ete0cN7QIDAQAB";
        	
        	if (string != null) {
        		base64EncodedPublicKey = string;
        	}
        	
            PublicKey key = Security.generatePublicKey(base64EncodedPublicKey);
            verified = Security.verify(key, signedData, signature);
            if (!verified) {
                Log.w(TAG, "signature does not match data.");
                return null;
            }
        }

        JSONObject jObject;
        JSONArray jTransactionsArray = null;
        int numTransactions = 0;
        long nonce = 0L;
        try {
            jObject = new JSONObject(signedData);

            // The nonce might be null if the user backed out of the buy page.
            nonce = jObject.optLong("nonce");
            jTransactionsArray = jObject.optJSONArray("orders");
            if (jTransactionsArray != null) {
                numTransactions = jTransactionsArray.length();
            }
        } catch (JSONException e) {
            return null;
        }

        if (!Security.isNonceKnown(nonce)) {
            Log.w(TAG, "Nonce not found: " + nonce);
            return null;
        }

        ArrayList<VerifiedPurchase> purchases = new ArrayList<VerifiedPurchase>();
        try {
            for (int i = 0; i < numTransactions; i++) {
                JSONObject jElement = jTransactionsArray.getJSONObject(i);
                int response = jElement.getInt("purchaseState");
                PurchaseState purchaseState = PurchaseState.valueOf(response);
                String productId = jElement.getString("productId");
                //String packageName = jElement.getString("packageName");
                long purchaseTime = jElement.getLong("purchaseTime");
                String orderId = jElement.optString("orderId", "");
                String notifyId = null;
                if (jElement.has("notificationId")) {
                    notifyId = jElement.getString("notificationId");
                }
                String developerPayload = jElement.optString("developerPayload", null);

                // If the purchase state is PURCHASED, then we require a
                // verified nonce.
                //if (purchaseState == PurchaseState.PURCHASED && !verified) {
                //    continue;
                //}
                purchases.add(new VerifiedPurchase(purchaseState, notifyId, productId,
                        orderId, purchaseTime, developerPayload));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception: ", e);
            return null;
        }
        removeNonce(nonce);
        return purchases;
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "Invalid key specification.");
            throw new IllegalArgumentException(e);
        } catch (Base64DecoderException e) {
            Log.e(TAG, "Base64 decoding failed.");
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    public static boolean verify(PublicKey publicKey, String signedData, String signature) {
        if (Consts.DEBUG) {
            Log.i(TAG, "signature: " + signature);
        }
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                Log.e(TAG, "Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key specification.");
        } catch (SignatureException e) {
            Log.e(TAG, "Signature exception.");
        } catch (Base64DecoderException e) {
            Log.e(TAG, "Base64 decoding failed.");
        }
        return false;
    }
}
