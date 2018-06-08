package com.zhl.rxjavanotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 操作符zip
     */
    private void sample6() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "observable1: emit -> 1");
                e.onNext(1);
                Log.d(TAG, "observable1: emit -> 2");
                e.onNext(2);
                Log.d(TAG, "observable1: emit -> 3");
                e.onNext(3);
                Log.d(TAG, "observable1: onComplete");
                e.onComplete();
            }
        });

        Observable<String> Observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "observable2: emit -> A");
                e.onNext("A");
                Log.d(TAG, "observable2: emit -> B");
                e.onNext("B");
                Log.d(TAG, "observable2: emit -> C");
                e.onNext("C");
                Log.d(TAG, "observable2: onComplete");
                e.onComplete();
            }
        });

        Observable.zip(observable1, Observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(@NonNull String s) {
                Log.d(TAG, "onNext:" + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "onError:");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete:");
            }
        });
    }

    /**
     * 操作符concatMap
     */
    private void sample5() {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        })
                .concatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                        ArrayList<String> list = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            list.add("This is concatMap " + integer + " -> value " + i);
                        }
                        return Observable.fromIterable(list).delay(100, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "accept: " + s);
                    }
                });
    }

    /**
     * 操作符flatMap
     */
    private void sample4() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        })
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                        ArrayList<String> list = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            list.add("This is flatMap " + integer + " -> value = " + i);
                        }
                        return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "accept: " + s);
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    /**
     * 变换操作符map
     */
    private void sample3() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "subscribe: " + " -> " + Thread.currentThread().getName());
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        Log.d(TAG, "apply: " + integer + " -> " + Thread.currentThread().getName());
                        return "This is map result" + integer;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "accept: " + s + " -> " + Thread.currentThread().getName());
                    }
                });
        mCompositeDisposable.add(disposable);
    }

    /**
     * 网络请求，读取数据库，切换线程
     */
    private void sample2() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "sample2() -> subscribe: " + Thread.currentThread().getName());
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        })
                .observeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "sample2() -> doOnNext -> accept: " + integer + Thread.currentThread()
                                .getName());
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "sample2() -> subscribe -> accept: " + integer + Thread.currentThread
                                ().getName());
                    }
                });

        mCompositeDisposable.add(disposable);
    }

    /**
     * 简单的上游通知下游
     */
    private void sample1() {
        //创建一个上游Observable.
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(0);
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });

        //创建一个下游Observer.
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mCompositeDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Log.d(TAG, "sample1() -> onNext: " + integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d("sample", "onComplete()结束");
            }
        };
        //建立连接
        observable.subscribeOn(Schedulers.newThread()).subscribe(observer);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}
