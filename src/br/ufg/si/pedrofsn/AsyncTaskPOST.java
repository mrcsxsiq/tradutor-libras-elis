package br.ufg.si.pedrofsn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import br.ufg.si.pedrofsn.model.ELiS;

public class AsyncTaskPOST extends AsyncTask<String, Void, String> {

	public interface InterfaceAsyncTaskPostCallback {
		public void postRealizado(String retornoDoServidor);
	}

	private InterfaceAsyncTaskPostCallback callback;
	private ELiS elis;

	public AsyncTaskPOST(InterfaceAsyncTaskPostCallback callback, ELiS elis) {
		this.elis = elis;
		this.callback = callback;
	}

	@Override
	protected String doInBackground(String... urls) {
		return post(urls[0]);
	}

	@Override
	protected void onPostExecute(String retornoDoServidor) {
		callback.postRealizado(retornoDoServidor);
	}

	private String post(String url) {
		InputStream inputStream = null;
		String resultado = "";
		try {

			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost(url);

			String jsonEmString = "";

			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("de", elis.getDe());
			jsonObject.accumulate("para", elis.getPara());
			jsonObject.accumulate("termo", elis.getTermo());

			jsonEmString = jsonObject.toString();

			StringEntity stringEntity = new StringEntity(jsonEmString);

			httpPost.setEntity(stringEntity);

			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			inputStream = httpResponse.getEntity().getContent();

			if (inputStream != null)
				resultado = converterInputStreamParaString(inputStream);
			else
				resultado = "Ocorreu um problema!";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultado;
	}

	private String converterInputStreamParaString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String linhaAtual = "";
		String resultado = "";
		while ((linhaAtual = bufferedReader.readLine()) != null)
			resultado += linhaAtual;

		inputStream.close();
		return resultado;

	}
}
