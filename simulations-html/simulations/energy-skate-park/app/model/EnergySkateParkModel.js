//Complete model for Energy Skate Park
define( ['model/vector2d', 'model/Skater', 'model/Property', 'model/BooleanProperty'], function ( Vector2D, Skater, Property, BooleanProperty ) {

    function EnergySkateParkModel() {
        this.skater = new Skater();
        this.barChartVisible = new BooleanProperty( false );
        this.pieChartVisible = new BooleanProperty( false );
        this.gridVisible = new BooleanProperty( false );
        this.speedometerVisible = new BooleanProperty( false );
        this.playing = new BooleanProperty( true );
        this.slowMotion = new BooleanProperty( false );

        //Pixels
        this.groundHeight = 116;
        this.groundY = 768 - this.groundHeight;
    }

    EnergySkateParkModel.prototype.resetAll = function () {
//        this.skater.reset();
        this.barChartVisible.reset();
        this.pieChartVisible.reset();
        this.gridVisible.reset();
        this.speedometerVisible.reset();
        this.playing.reset();
        this.slowMotion.reset();
    };

    return EnergySkateParkModel;
} );