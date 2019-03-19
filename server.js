require( 'dotenv' ).config();
const wallpaper = require( 'wallpaper' );
const path = require( 'path' );
const fs = require( 'fs' );

const folder = process.env.folder;
const delay = ( process.env.delay || 60 ) * 1000;

setInterval( () => {
  changeWallpaper();
}, delay );

async function changeWallpaper() {
  return new Promise( async resolve => {
    const item = await getRandomWallpaper();
    if( item === false ) {
      resolve();
    }

    await wallpaper.set( path.join( folder, item ) );

    console.log( item );

    resolve();
  } );
}

async function getRandomWallpaper() {
  return new Promise( async resolve => {
    fs.readdir( folder, async ( error, items ) => {

      if ( error ) {
        console.error( 'error reading directory', error );
        resolve( false );
      }

      // get current wallpaper
      const current = path.basename( await wallpaper.get() );

      // filter current from list of available
      items = items.filter( x => x !== current );

      // randomly choose a single wallpaper
      var item = items[ Math.floor( Math.random() * items.length ) ];

      // resolve it back
      resolve( item );
    });
  } );
}

( async () => {
  await changeWallpaper();
} )();