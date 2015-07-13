var appDispatcher = new Backbone.Events();

var CarsSearchEvents = {
    UPDATE_BRAND: 'UPDATE_BRAND',
    UPDATE_MODEL: 'UPDATE_MODEL',
    CHANGE: 'CHANGE'
};

var CarsSearchActions = {
    updateBrand: function(brandId) {
        appDispatcher.dispatch(CarsSearchEvents.UPDATE_BRAND, {
            brandId: brandId
        });
    },

    updateModel: function(modelId) {
        appDispatcher.dispatch(CarsSearchEvents.UPDATE_MODEL, {
            modelId: modelId
        });
    }
};

// Singletons... would be nicer with DI
var carsSearchBrandsStore = new CarsSearchBrandsStore();
var carsSearchModelsStore = new CarsSearchModelsStore();
var carsSearchStore = new CarsSearchStore();

function CarsSearchBrandsStore() {
    var _items = [
        { id: '1', label: 'Ford' },
        { id: '2', label: 'Audi' }
    ];

    this.eventEmitter = new Backbone.Events();

    this.getAll = function() {
        return _items;
    };

    this.getById = function(id) {
        return _.find(_items, function(item) {
            return item.id === id;
        });
    };
}

function CarsSearchModelsStore() {
    var _items = [];

    this.eventEmitter = new Backbone.Events();

    this.getAll = function() {
        return _items;
    };

    appDispatcher.addEventListener(CarsSearchEvents.UPDATE_BRAND, _.bind(function(event) {
        var brand;

        if (event.brandId) {
            brand = carsSearchBrandsStore.getById(event.brandId);
        }

        if (brand) {
            _items = [
                { id: '1', label: brand.label + ' model X' },
                { id: '2', label: brand.label + ' model Y' }
            ];
        } else {
            _items = [];
        }

        this.eventEmitter.dispatch(CarsSearchEvents.CHANGE);
    }, this));
}

function CarsSearchStore() {
    var _brandId = '';
    var _modelId = '';

    this.eventEmitter = new Backbone.Events();

    this.getBrandId = function() {
        return _brandId;
    };

    this.getModelId = function() {
        return _modelId;
    };

    appDispatcher.addEventListener(CarsSearchEvents.UPDATE_BRAND, _.bind(function(event) {
        _brandId = event.brandId;
        _modelId = '';
        this.eventEmitter.dispatch(CarsSearchEvents.CHANGE);
    }, this));

    appDispatcher.addEventListener(CarsSearchEvents.UPDATE_MODEL, _.bind(function(event) {
        _modelId = event.modelId;
        this.eventEmitter.dispatch(CarsSearchEvents.CHANGE);
    }, this));
}

var SelectBox = React.createClass({displayName: "SelectBox",
    propTypes: {
        options: React.PropTypes.array.isRequired,
        valueProp: React.PropTypes.string.isRequired,
        labelProp: React.PropTypes.string.isRequired,
        placeholderLabel: React.PropTypes.string.isRequired,
        initialValue: React.PropTypes.string,
        onChange: React.PropTypes.func
    },

    getInitialState: function() {
        return {
            value: this.props.initialValue
        };
    },

    onChange: function(event) {
        var value = event.target.value;

        console.log('SelectBox#onChange', value);

        this.setState({
            value: value
        });

        if (this.props.onChange) {
            this.props.onChange(value);
        }
    },

    render: function() {
        return React.createElement("select", {value: this.state.value, onChange: this.onChange},
            React.createElement("option", {value: ""}, this.props.placeholderLabel),
            this.props.options.map(_.bind(function(option) {
                return React.createElement("option", {value: option[this.props.valueProp], key: option[this.props.valueProp]}, option[this.props.labelProp]);
            }, this))
        );
    }
});

var Form = React.createClass({displayName: "Form",
    onSubmit: function(event) {
        event.preventDefault();

        console.log('Form#onSubmit', this);
    },

    render: function() {
        return React.createElement("form", {onSubmit: this.onSubmit},
            this.props.children
        );
    }
});

var FormGroup = React.createClass({displayName: "FormGroup",
    propTypes: {
        label: React.PropTypes.string.isRequired
    },

    render: function() {
        return React.createElement("div", null,
            React.createElement("label", null,
                this.props.label,
                this.props.children
            )
        );
    }
});

var Button = React.createClass({displayName: "Button",
    propTypes: {
        context: React.PropTypes.oneOf(['primary', 'secondary']),
        size: React.PropTypes.oneOf(['small', 'large']),
        isDisabled: React.PropTypes.bool
    },

    getClassName: function() {
        var classes = [
            'button',
            this.props.context,
            this.props.size
        ];

        if (this.props.isDisabled) {
            classes.push('disabled');
        }

        return classes.join(' ');
    },

    render: function() {
        return React.createElement("button", {className: this.getClassName(), disabled: this.props.isDisabled},
            this.props.children
        )
    }
});

function getL1CarsSearchPanelState() {
    return {
        brands: carsSearchBrandsStore.getAll(),
        models: carsSearchModelsStore.getAll(),
        selectedBrandId: carsSearchStore.getBrandId(),
        selectedModelId: carsSearchStore.getModelId()
    };
}

var L1CarsSearchPanel = React.createClass({displayName: "L1CarsSearchPanel",
    getInitialState: function() {
        return getL1CarsSearchPanelState();
    },

    componentDidMount: function() {
        carsSearchBrandsStore.eventEmitter.addEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
        carsSearchModelsStore.eventEmitter.addEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
        carsSearchStore.eventEmitter.addEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
    },

    componentWillUnmount: function() {
        carsSearchBrandsStore.eventEmitter.removeEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
        carsSearchModelsStore.eventEmitter.removeEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
        carsSearchStore.eventEmitter.removeEventListener(CarsSearchEvents.CHANGE, this.onStoreChange, this);
    },

    onStoreChange: function() {
        this.setState(getL1CarsSearchPanelState());
    },

    onBrandSelect: function(id) {
        CarsSearchActions.updateBrand(id);
    },

    onModelSelect: function(id) {
        CarsSearchActions.updateModel(id);
    },

    canSubmit: function() {
        return !this.state.selectedBrandId || !this.state.selectedModelId;
    },

    brandPlaceholderLabel: 'Alle merken ...',

    getModelPlaceholderLabel: function() {
        if (this.state.selectedBrandId) {
            return 'Alle modellen ...'
        } else {
            return 'Kies eerst een merk';
        }
    },

    render: function() {
        return React.createElement("div", {id: "l1-cards-buy-block"},
            React.createElement("div", {id: "l1-cars-search-block"},
                React.createElement("div", {id: "l1-cars-search-block-title"}, "Auto zoeken"),
                React.createElement("div", {id: "l1-cars-search-block-content"},
                    React.createElement(Form, null,
                        React.createElement(FormGroup, {label: "Merk"},
                            React.createElement(SelectBox, {options: this.state.brands, valueProp: "id", labelProp: "label", placeholderLabel: this.brandPlaceholderLabel, initialValue: this.state.selectedBrandId, onChange: this.onBrandSelect})
                        ),

                        React.createElement(FormGroup, {label: "Model"},
                            React.createElement(SelectBox, {options: this.state.models, valueProp: "id", labelProp: "label", placeholderLabel: this.getModelPlaceholderLabel(), initialValue: this.state.selectedModelId, onChange: this.onModelSelect})
                        ),

                        React.createElement(Button, {context: "primary", size: "large", isDisabled: this.canSubmit()}, "Zoeken")
                    )
                )
            )
        );
    }
});
var renderServer = function () {
    return React.renderToString(React.createElement(L1CarsSearchPanel));
};