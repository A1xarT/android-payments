package com.example.AndroidPaymentApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeysBundle {
    private List<KeysEntity> keys;

    public void setKeys(List<KeysEntity> keys) {
        this.keys = keys;
    }

    public List<KeysEntity> getKeys() {
        return keys;
    }

    public List<String> getPublicKeys() {
        return keys.stream().map(KeysEntity::getPublicKey).collect(Collectors.toList());
    }

    public KeysBundle(List<KeysEntity> keys) {
        this.keys = new ArrayList<>(keys);
    }

    public void addKeysPair(KeysEntity keysEntity) {
        if (Objects.equals(keysEntity.getPublicKey(), BuildConfig.REVOLUT_MERCHANT_API_KEY)) return;
        List<String> publicKeys = getPublicKeys();
        if (!publicKeys.contains(keysEntity.getPublicKey())) {
            keys.add(keysEntity);
        }
    }
    public void initDefaultPair(KeysEntity keysEntity){
        keys.removeIf(x -> x.getPublicKey().equals(keysEntity.getPublicKey()));
        keys.add(keysEntity);
    }

    public void removeKeysPair(KeysEntity keysEntity) {
        if (keysEntity != null)
            this.keys.remove(keysEntity);
    }

    public void removeKeysPairByPublicKey(String publicKey) {
        if (Objects.equals(publicKey, BuildConfig.REVOLUT_MERCHANT_API_KEY)) return;
        keys = keys.stream().filter(x -> !Objects.equals(publicKey, x.getPublicKey())).collect(Collectors.toList());
    }
}
