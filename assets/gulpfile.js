var gulp    = require('gulp');
// var uglify  = require('gulp-uglify');
var rename  = require('gulp-rename');
// var concat  = require('gulp-concat');
// var coffee  = require('gulp-coffee');
var sass    = require('gulp-sass');
// var streamqueue = require('streamqueue');

var webpack = require('gulp-webpack');

var webpackOptions = {
  output: {
    filename: 'application.min.js'
  },
  module: {
    loaders: [{
      test:/.jsx?$/,
      exclude: /node_modules/,
      loader: 'babel-loader',
      query: {
        presets: ['es2015'],
        plugins: [
          ["transform-react-jsx", { "pragma": "m"}]
        ]
      }
    }]
  }
}

gulp.task('webpack', function() {
  return gulp.src('application.js')
    .pipe(webpack(webpackOptions))
    // .pipe(uglify())
    .pipe(gulp.dest('../src/main/resources/assets'));
});


gulp.task('stylesheets', function () {
  var options = {
    includePaths: ['node_modules'],
    precision: 10
  };
  return gulp.src(['application.scss'])
    .pipe(sass(options).on('error', sass.logError))
    .pipe(rename('application.min.css'))
    .pipe(gulp.dest('../src/main/resources/assets'));
});

gulp.task('fonts', function() {
  return gulp.src([
    'node_modules/bootstrap-sass/assets/fonts/bootstrap/*',
    'node_modules/font-awesome/fonts/*'
  ])
  .pipe(gulp.dest('../src/main/resources/assets/fonts'));
});

gulp.task('images', function() {
  return gulp.src([
    'images/*',
  ])
  .pipe(gulp.dest('../src/main/resources/assets/images'));
});

gulp.task('watch', function() {
  // gulp.watch('application.js', ['webpack']);
  gulp.watch('application.scss', ['stylesheets']);
});



gulp.task('default', ['webpack', 'stylesheets']);
