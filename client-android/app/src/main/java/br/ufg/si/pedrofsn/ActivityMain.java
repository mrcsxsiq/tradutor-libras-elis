/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.ufg.si.pedrofsn;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import br.ufg.si.pedrofsn.Utils.Navegacao;
import br.ufg.si.pedrofsn.Utils.Utils;
import br.ufg.si.pedrofsn.teclado.Constantes;
import br.ufg.si.pedrofsn.teclado.enums.TipoLingua;
import br.ufg.si.pedrofsn.teclado.fragments.FragmentElisKeyboard;
import br.ufg.si.pedrofsn.teclado.fragments.FragmentTopoTradutor;
import br.ufg.si.pedrofsn.teclado.interfaces.CallbackFragmentToActivity;
import br.ufg.si.pedrofsn.teclado.interfaces.IElisKeyboard;
import br.ufg.si.pedrofsn.teclado.models.Termo;
import br.ufg.si.pedrofsn.teclado.models.Visografema;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ActivityMain extends AppCompatActivity implements CallbackFragmentToActivity, IElisKeyboard {

    private FrameLayout frameLayoutKeyboardElis;
    private FrameLayout frameLayoutTelaTradutor;
    private TextView textViewElis;
    private List<Visografema> listaDeVisografemasPressionados = new ArrayList<Visografema>();
    private FragmentResultado fragmentResultado;
    private int caso;

    /////////////////////// ACTIVITY

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTaskPOST(this, null).execute(Url.URL + "busca");

        frameLayoutTelaTradutor = (FrameLayout) findViewById(R.id.frameLayoutTelaTradutor);
        frameLayoutKeyboardElis = (FrameLayout) findViewById(R.id.frameLayoutKeyboardElis);

        // Inicia com o keyboard-elis invisível, se for com GONE, o getSupportFragmentManager() não vai conseguir pegar a referência depois, mesmo se der VISIBLE. Esta é uma solução paleativa.
        frameLayoutKeyboardElis.setVisibility(View.INVISIBLE);

        Navegacao.showFragmentInicial(new FragmentTopoTradutor(), getSupportFragmentManager(), FragmentTopoTradutor.TAG, R.id.frameLayoutTelaTradutor);
        Navegacao.showFragmentInicial(new FragmentElisKeyboard(), getSupportFragmentManager(), FragmentElisKeyboard.TAG, R.id.frameLayoutKeyboardElis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sobre:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(Html.fromHtml(getString(R.string.dialog_sobre))).setTitle(R.string.sobre).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /////////////////////// SOLUÇÕES DOS FRAGMENT

    public FrameLayout getElisKeyboard() {
        return frameLayoutKeyboardElis;
    }

    public List<Visografema> getListaVisografemasInputados() {
        return listaDeVisografemasPressionados;
    }

    @Override
    public void getTextViewElisNaActivity(TextView v) {
        textViewElis = (TextView) v;
    }

    @Override
    public void onBotaoVisografemaPressionado(Visografema visografema) {
        if (Constantes.isSobrescritoPressionado) {
            visografema.setValorElis(visografema.getValorElis());
            visografema.setSobrescrito(true);
        } else if (Constantes.isSublinhadoPressionado) {
            visografema.setValorElis(visografema.getValorElis());
            visografema.setSublinhado(true);
        }

        listaDeVisografemasPressionados.add(visografema);

        renderizarElis();
    }

    private void renderizarElis() {
        String conteudoDigitado = "";
        for (Visografema v : listaDeVisografemasPressionados) {
            if (v.isSublinhado() && v.isSobrescrito()) {
                conteudoDigitado += "<sup><s>" + v.getValorElis() + "</s></sup>";
            } else if (v.isSublinhado()) {
                conteudoDigitado += "<s>" + v.getValorElis() + "</s>";
            } else if (v.isSobrescrito()) {
                conteudoDigitado += "<sup>" + v.getValorElis() + "</sup>";
            } else {
                conteudoDigitado += v.getValorElis();
            }
        }

        textViewElis.setText(Html.fromHtml("<head></head><body>" + conteudoDigitado + "</body>"));

        Constantes.isSobrescritoPressionado = false;
        Constantes.isSublinhadoPressionado = false;

        Utils.aplicarFonteElis(this, textViewElis);
    }

    @Override
    public void onBotaoEspacoPressionado() {
        listaDeVisografemasPressionados.add(new Visografema(" "));
    }

    @Override
    public void onBotaoBackspacePressionado() {
        if (listaDeVisografemasPressionados != null && listaDeVisografemasPressionados.size() > 0) {
            listaDeVisografemasPressionados.remove(listaDeVisografemasPressionados.size() - 1);
            renderizarElis();
        }
    }

    @Override
    public void onBotaoTraduzirTermoPressionado(Termo termoResultado) {
        if (termoResultado != null) {
            if (termoResultado.getTipoLingua() == TipoLingua.PTBR) {
                frameLayoutKeyboardElis.setVisibility(View.VISIBLE);
            }

            ocultarCardResultado();

            Bundle bundle = new Bundle();
            bundle.putSerializable("resultado", termoResultado);
            fragmentResultado = new FragmentResultado();
            fragmentResultado.setArguments(bundle);

            Navegacao.replaceFragment(fragmentResultado, getSupportFragmentManager(), FragmentResultado.TAG, R.id.frameLayoutKeyboardElis);
        }
    }

    @Override
    public void setErrorMessage(String mensagem) {
        Crouton.makeText(this, mensagem, Style.ALERT).show();
    }

    public void ocultarCardResultado() {
        // Permite traduzir outro termo sem ter que apertar o botão ok do card de resultado
        if (fragmentResultado != null) {
            Navegacao.detachFragment(fragmentResultado, getSupportFragmentManager());
        }
    }

    public void setTipoTraducao(int caso) {
        // caso 0 = PTBR > ELiS
        // caso 1 = ELiS > PTBR

        this.caso = caso;
    }

    public int getCaso() {
        return caso;
    }
}
