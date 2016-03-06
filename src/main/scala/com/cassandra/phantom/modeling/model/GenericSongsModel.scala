package com.cassandra.phantom.modeling.model

import java.util.UUID

import com.cassandra.phantom.modeling.entity.Song
import com.websudos.phantom.dsl._

import scala.concurrent.Future


class SongsModel extends CassandraTable[ConcreteSongsModel, Song] {

  object id extends TimeUUIDColumn(this) with PartitionKey[UUID]

  object artist extends StringColumn(this)

  object title extends StringColumn(this)
  object album extends StringColumn(this)

  override def fromRow(r: Row): Song = {
    Song(
      id(r),
      title(r),
      album(r),
      artist(r)
    )
  }
}

abstract class ConcreteSongsModel extends SongsModel with RootConnector {

  def store(song: Song): Future[ResultSet] = {
    insert
      .value(_.id, song.id)
      .value(_.title, song.title)
      .value(_.album, song.album)
      .value(_.artist, song.artist)
      .future()
  }

  def getBySongId(id: UUID): Future[Option[Song]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

}

class SongsByArtistModel extends CassandraTable[SongsByArtistModel, Song] {
  object artist extends StringColumn(this) with PartitionKey[String]
  object id extends TimeUUIDColumn(this) with ClusteringOrder[UUID]
  object title extends StringColumn(this)
  object album extends StringColumn(this)

  override def fromRow(r: Row): Song = {
    Song(
      id(r),
      title(r),
      album(r),
      artist(r)
    )
  }

}

abstract class ConcreteSongsByArtistModel extends SongsByArtistModel with RootConnector {
  def store(songs: Song): Future[ResultSet] = {
    insert
      .value(_.id, songs.id)
      .value(_.title, songs.title)
      .value(_.album, songs.album)
      .value(_.artist, songs.artist)
      .future()
  }

  def getByArtist(artist: String): Future[List[Song]] = {
    select.where(_.artist eqs artist).fetch()
  }

  def deleteByArtistAndId(artist: String, id: UUID): Future[ResultSet] = {
    delete.where(_.artist eqs artist).and(_.id eqs id).future()
  }
}