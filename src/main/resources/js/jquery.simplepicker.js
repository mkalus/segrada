/*
 * Very simple jQuery Picker
 *
 * Original project was made exclusively for colors. Enhanced to be used for
 * any <select> element that has fixed sized options.
 *
 * https://github.com/tkrotoff/jquery-simplecolorpicker
 * Copyright (C) 2012-2013 Tanguy Krotoff <tkrotoff@gmail.com>
 *
 * https://github.com/ushahidi/jquery-simplepicker
 * Copyright (C) 2014 Ushahidi <http://ushahidi.com/>
 *
 * Licensed under the MIT license
 */

(function($) {
    'use strict';

    /**
     * Constructor.
     */
    var SimplePicker = function(select, options) {
        this.init('simplepicker', select, options);
    };

    /**
     * SimplePicker class.
     */
    SimplePicker.prototype = {
        constructor: SimplePicker,

        init: function(type, select, options) {
            var self = this;

            self.type = type;

            self.$select = $(select);
            self.$select.hide();

            self.options = $.extend({}, $.fn.simplepicker.defaults, options);

            if (!self.options.setIconValue) {
                // default to using the option callback for icons
                self.options.setIconValue = self.options.setOptionValue;
            }

            self.$optionList = null;

            if (self.options.picker === true) {
                var selectText = self.$select.find('> option:selected').text();
                self.$icon = $('<span class="simplepicker icon"'
                + ' title="' + selectText + '"'
                + ' role="button" tabindex="0">'
                + '</span>').insertAfter(self.$select);
                self.$icon.on('click.' + self.type, $.proxy(self.showPicker, self));
                self.$icon.on('keydown.' + self.type, function(e) {
                    if (e.which === 13) {
                        self.showPicker();
                    }
                });

                // allow the user to override how the selected icon functions
                self.options.setIconValue(self.$icon, self.$select.val());

                self.$picker = $('<span class="simplepicker picker ' + self.options.theme + '"></span>').appendTo(document.body);
                self.$optionList = self.$picker;

                // Hide picker when clicking outside
                $(document).on('mousedown.' + self.type, $.proxy(self.hidePicker, self));
                self.$picker.on('mousedown.' + self.type, $.proxy(self.mousedown, self));
                self.$picker.on('keydown.' + self.type, function(e) {
                    if (e.which === 27) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.hidePicker();
                    }
                });
            } else {
                self.$inline = $('<span class="simplepicker inline ' + self.options.theme + '"></span>').insertAfter(self.$select);
                self.$optionList = self.$inline;
            }

            // Build the list of options
            // <span class="option selected" title="Foo" role="button"></span>
            self.$select.find('> option').each(function() {
                var $option = $(this);
                var value = $option.val();

                var isSelected = $option.is(':selected');
                var isDisabled = $option.is(':disabled');

                var selected = '';
                if (isSelected === true) {
                    selected = ' data-selected';
                }

                var disabled = '';
                if (isDisabled === true) {
                    disabled = ' data-disabled';
                }

                var title = '';
                if (isDisabled === false) {
                    title = ' title="' + $option.text() + '"';
                }

                var role = '';
                if (isDisabled === false) {
                    role = ' role="button" tabindex="0"';
                }

                var $optionSpan = $('<span class="option"'
                + title
                + ' data-value="' + value + '"'
                + selected
                + disabled
                + role + '>'
                + '</span>');

                // allow the user to override how values are rendered into options
                self.options.setOptionValue($optionSpan, value);

                self.$optionList.append($optionSpan);
                $optionSpan.on('click.' + self.type, $.proxy(self.optionSpanClicked, self));
                $optionSpan.on('keydown.' + self.type, function(e) {
                    if (e.which === 13) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.optionSpanClicked(e);
                    }
                });

                var $next = $option.next();
                if ($next.is('optgroup') === true) {
                    // Vertical break, like hr
                    self.$optionList.append('<span class="vr"></span>');
                }
            });

            // This sets the focus to the first button in the picker dialog
            // It also enables looping of the tabs both forward and reverse direction.
            if (self.options.picker === true) {
                var $buttons = self.$picker.find('[role=button]');
                var $firstButton = $buttons.first();
                var $lastButton = $buttons.last();

                $firstButton.on('keydown', function(e) {
                    if (e.which === 9 && e.shiftKey) {
                        e.preventDefault();
                        $lastButton.focus();
                    }
                });

                $lastButton.on('keydown', function(e) {
                    if (e.which === 9 && !e.shiftKey) {
                        e.preventDefault();
                        $firstButton.focus();
                    }
                });
            }
        },

        /**
         * Changes the selected option.
         *
         * @param value  to be selected
         */
        selectOption: function(value) {
            var self = this;

            var $optionSpan = self.$optionList.find('> span.option').filter(function() {
                return $(this).data('value').toLowerCase() === value.toLowerCase();
            });

            if ($optionSpan.length > 0) {
                self.selectOptionSpan($optionSpan);
            } else {
                console.error("The given option '" + value + "' could not be found");
            }
        },

        showPicker: function() {
            var self = this;
            var pos = this.$icon.offset();
            this.$picker.css({
                // Remove some pixels to align the picker icon with the icons inside the dropdown
                left: pos.left - 6,
                top: pos.top + this.$icon.outerHeight()
            });

            this.$picker.show(this.options.pickerDelay, function() {
                self.$picker.find('[role=button]').first().focus();
            });
        },

        hidePicker: function() {
            var self = this;
            var isVisible = this.$picker.is(":visible");
            if (isVisible) {
                this.$picker.hide(this.options.pickerDelay, function() {
                    self.$icon.focus();
                });
            }
        },

        /**
         * Selects the given span inside $optionList.
         *
         * The given span becomes the selected one.
         * It also changes the HTML select value, this will emit the 'change' event.
         */
        selectOptionSpan: function($optionSpan) {
            var value = $optionSpan.data('value');
            var title = $optionSpan.prop('title');

            // Mark this span as the selected one
            $optionSpan.siblings().removeAttr('data-selected');
            $optionSpan.attr('data-selected', '');

            if (this.options.picker === true) {
                this.options.setIconValue(this.$icon, value);
                this.$icon.prop('title', title);
                this.hidePicker();
            }

            // Change HTML select value
            this.$select.val(value);
        },

        /**
         * The user clicked on a value inside $optionList.
         */
        optionSpanClicked: function(e) {
            var $option = $(e.currentTarget);
            // When a value is clicked, make it the new selected one (unless disabled)
            if ($option.is('[data-disabled]') === false) {
                this.selectOptionSpan($option);
                this.$select.trigger('change');
            }
        },

        /**
         * Prevents the mousedown event from "eating" the click event.
         */
        mousedown: function(e) {
            e.stopPropagation();
            e.preventDefault();
        },

        destroy: function() {
            if (this.options.picker === true) {
                this.$icon.off('.' + this.type);
                this.$icon.remove();
                $(document).off('.' + this.type);
            }

            this.$optionList.off('.' + this.type);
            this.$optionList.remove();

            this.$select.removeData(this.type);
            this.$select.show();
        }
    };

    /**
     * Plugin definition.
     * How to use: $('#id').simplepicker()
     */
    $.fn.simplepicker = function(option) {
        var args = $.makeArray(arguments);
        args.shift();

        // For HTML element passed to the plugin
        return this.each(function() {
            var $this = $(this),
                data = $this.data('simplepicker'),
                options = typeof option === 'object' && option;
            if (data === undefined) {
                $this.data('simplepicker', (data = new SimplePicker(this, options)));
            }
            if (typeof option === 'string') {
                data[option].apply(data, args);
            }
        });
    };

    /**
     * Default options.
     */
    $.fn.simplepicker.defaults = {
        // No theme by default
        theme: '',

        // Show the picker or make it inline
        picker: false,

        // Animation delay in milliseconds
        pickerDelay: 0,

        // Change the icon value, defaults to setOptionValue
        setIconValue: null,

        // Change an option value
        setOptionValue: function ($option, value) {
            $option.addClass('empty').css('background-color', value);
        }

    };

})(jQuery);
