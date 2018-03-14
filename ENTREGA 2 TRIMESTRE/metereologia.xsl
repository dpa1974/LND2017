<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
  <html>
      <head>
      <style>
        table, td, th, tr {border:1px solid blue;text-align:center;bgcolor:grey}
        h2 {color:blue}
      </style>
    </head>
    <body>
    <h2>Resumen del Dia 13/02/2018</h2>
      <table>
      <tr>
        <th style="text-align:center">TEMP MAX</th>
        <th style="text-align:center">TEMP MIN</th>
        <th style="text-align:center">HUMEDAD</th>
        <th style="text-align:center">PRESION ATM</th>
        <th style="text-align:center">PRECIPITACIONES</th>
      </tr>
      <xsl:for-each select="//medicion [@fecha='2018-02-13']">
      <tr>
        <td><xsl:value-of select="//temperatura/t_max"/></td>
        <td><xsl:value-of select="//temperatura/t_min"/></td>
        <td><xsl:value-of select="humedad"/></td>
        <td><xsl:value-of select="presion_atm"/></td>
        <td bgcolor="#e5e3e3"><xsl:value-of select="presipitaciones"/></td>
      </tr>
      </xsl:for-each>
    </table>
    <h2>Tabla de Mareas</h2>
    <h3>Plenamar &amp; Bajamar</h3>
    <xsl:for-each select="//medicion [@fecha='2018-02-13']/mareas">
      <ul>
        <li><xsl:value-of select="plenamar"/></li>
        <li><xsl:value-of select="bajamar"/></li>
      </ul>
    </xsl:for-each>
      <h2>Mayores Rachas de Viento por día</h2>
      <table>
      <tr>
        <th style="text-align:center">DIRECCION</th>
        <th style="text-align:center">RACHA MAX</th>
      </tr>
      <xsl:for-each select="//medicion/viento">
      <tr>
      <td><xsl:value-of select="direccion"/></td>
      <xsl:choose>
        <xsl:when test="racha_max &gt; 35">
          <td bgcolor="#ff00ff">
          <xsl:value-of select="racha_max"/></td>
        </xsl:when>
        <xsl:otherwise>
          <td><xsl:value-of select="racha_max"/></td>
        </xsl:otherwise>
      </xsl:choose>
      </tr>
      </xsl:for-each>
    </table>
     <h2>Humedad media del mes</h2>
    <table>
    <tr>
		<td>Media</td>
		<th><xsl:value-of select="sum(//humedad) div count(//humedad)"/></th>
    </tr>
    </table>
  </body>
</html>
  </xsl:template>
</xsl:stylesheet>
