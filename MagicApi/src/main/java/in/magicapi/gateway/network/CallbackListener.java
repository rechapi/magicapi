package in.magicapi.gateway.network;

public interface CallbackListener {
    public void onResult(Exception e, String result, ResponseManager responseManager);
}
