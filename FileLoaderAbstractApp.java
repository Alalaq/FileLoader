public abstract class FileLoaderAbstractApp {
        protected FileLoaderAbstractApp(){
            initialize();
            start();
        }

        protected abstract void initialize();
        protected abstract void start();
}
